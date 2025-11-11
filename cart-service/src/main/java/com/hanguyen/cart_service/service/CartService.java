package com.hanguyen.cart_service.service;

import com.hanguyen.cart_service.configration.RabbitMQConfig;
import com.hanguyen.cart_service.configration.RabbitMQProperties;
import com.hanguyen.cart_service.dto.ApiResponse;
import com.hanguyen.cart_service.dto.ProductInCart;
import com.hanguyen.cart_service.dto.event.CartSyncDataEvent;
import com.hanguyen.cart_service.dto.request.CartRequest;
import com.hanguyen.cart_service.dto.response.CartResponse;
import com.hanguyen.cart_service.dto.response.InventoryResponse;
import com.hanguyen.cart_service.dto.response.ProductResponse;
import com.hanguyen.cart_service.entity.CartDocument;
import com.hanguyen.cart_service.exception.AppException;
import com.hanguyen.cart_service.exception.ErrorCode;
import com.hanguyen.cart_service.repository.CartRepository;
import com.hanguyen.cart_service.repository.client.InventoryClient;
import com.hanguyen.cart_service.repository.client.ProductClient;
import com.hanguyen.cart_service.utils.SecurityUtils;
import feign.FeignException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CartService {

    RedisTemplate<String, Object> redisTemplate;
    ProductClient productClient;
    InventoryClient inventoryClient;

    RabbitTemplate rabbitTemplate;
    CartRepository cartRepository;

    RabbitMQProperties rabbitMQProperties;

    static final String KEY_PREFIX = "cart:";
    static final long TTL_IN_DAYS = 7L;


    @CircuitBreaker(name = "productService", fallbackMethod = "fallbackAddProduct")
    public CartResponse addProductToCart(CartRequest request) {
        ProductResponse product ;
        try {
            ApiResponse<ProductResponse> productApiResponse = productClient.getProductByBookId(request.getBookId());
            product = (productApiResponse != null) ? productApiResponse.getResult() : null;
            if (product == null || product.getId() == null) {
                log.error("[CartService] Can not find product with id={} for userId={}", request.getBookId(), getCartKey());
                throw new AppException(ErrorCode.PRODUCT_NOT_FOUND);
            }
        }
        catch (FeignException e){
            throw new AppException(ErrorCode.PRODUCT_NOT_FOUND);
        }


        ApiResponse<InventoryResponse> inventoryResponse = inventoryClient.getStockById(request.getBookId());
        InventoryResponse inventory = (inventoryResponse != null) ? inventoryResponse.getResult() : null;
        if (inventory ==null || inventory.getBookId() == null ){
            throw new AppException(ErrorCode.QUANTITY_EXCEPTION);
        }
        if (inventory.getStock() < request.getQuantity()) {
            throw new AppException(ErrorCode.QUANTITY_EXCEPTION);
        }

        String key = getCartKey();
        HashOperations<String, String, CartRequest> hashOps = redisTemplate.opsForHash();

        CartRequest existing = hashOps.get(key, request.getBookId());
        int existQuantity = 0;
        if (existing != null) {
            existQuantity = existing.getQuantity();
            existing.setQuantity(existing.getQuantity() + request.getQuantity());
            hashOps.put(key, request.getBookId(), existing);
        } else {
            hashOps.put(key, request.getBookId(), request);
        }

        redisTemplate.expire(key, TTL_IN_DAYS, TimeUnit.DAYS);

        sendSyncDataEvent(ProductInCart.builder()
                .bookId(request.getBookId())
                .quantity(existQuantity + request.getQuantity())
                .instant(Instant.now())
                .build());

        return CartResponse.builder()
                .productResponse(product)
                .quantity(existQuantity + request.getQuantity())
                .build();
    }

    @CircuitBreaker(name = "productService", fallbackMethod = "fallbackGetCart")
    public List<CartResponse> getCart() {
        String key = getCartKey();
        HashOperations<String, String, CartRequest> hashOps = redisTemplate.opsForHash();
        Map<String, CartRequest> redisCart = hashOps.entries(key);

        if (!redisCart.isEmpty()) {
            log.info("Get hit product in cart from redis has key : {}" , key);
            redisTemplate.expire(key, TTL_IN_DAYS, TimeUnit.DAYS);
            List<CartResponse> responses = new ArrayList<>();

            for (Map.Entry<String, CartRequest> entry : redisCart.entrySet()) {
                String bookId = entry.getKey();
                CartRequest cartReq = entry.getValue();
                ProductResponse product;
                try {
                    ApiResponse<ProductResponse> productApiResponse = productClient.getProductByBookId(bookId);
                    product = (productApiResponse != null) ? productApiResponse.getResult() : null;
                }
                catch (FeignException e){
                    log.info("Product has id {} not found" , bookId);
                    product = null;
                }

                if (product != null) {
                    responses.add(CartResponse.builder()
                            .productResponse(product)
                            .quantity(cartReq.getQuantity())
                            .build());
                }
            }
            return responses;
        }
        log.info("Get miss from database");

        Optional<CartDocument> cartDocOpt = cartRepository.findByUserId(SecurityUtils.getUserId());
        if (cartDocOpt.isEmpty()) {
            return new ArrayList<>();
        }

        CartDocument cartDoc = cartDocOpt.get();
        List<ProductInCart> productInCarts = cartDoc.getProductInCarts();

        List<CartResponse> responses = new ArrayList<>();
        for (ProductInCart productInCart : productInCarts) {
            ProductResponse product;
            try {
                ApiResponse<ProductResponse> productApiResponse = productClient.getProductByBookId(productInCart.getBookId());
                product = (productApiResponse != null) ? productApiResponse.getResult() : null;
            }
            catch (FeignException e){
                log.info("Product has id {} not found when read data from db" , productInCart.getBookId());
                product = null;
            }
            if(product != null && productInCart.getInstant().isAfter(Instant.now().minus(7, ChronoUnit.DAYS))) {
                hashOps.put(key, productInCart.getBookId(), CartRequest.builder()
                        .bookId(productInCart.getBookId())
                        .quantity(productInCart.getQuantity()).build());

                responses.add(CartResponse.builder()
                            .productResponse(product)
                            .quantity(productInCart.getQuantity())
                            .build());
            }
        }
        redisTemplate.expire(key, TTL_IN_DAYS, TimeUnit.DAYS);
        return responses;
    }

    public CartResponse updateQuantity(CartRequest cartRequest) {
        ProductResponse product ;
        try {
            ApiResponse<ProductResponse> productApiResponse = productClient.getProductByBookId(cartRequest.getBookId());
            product = (productApiResponse != null) ? productApiResponse.getResult() : null;
            if (product == null || product.getId() == null) {
                log.error("[CartService] Can not find product with id={} for userId={} when update quantity", cartRequest.getBookId(), getCartKey());
                throw new AppException(ErrorCode.PRODUCT_NOT_FOUND);
            }
        }
        catch (FeignException e){
            throw new AppException(ErrorCode.PRODUCT_NOT_FOUND);
        }

        ApiResponse<InventoryResponse> inventoryResponse = inventoryClient.getStockById(cartRequest.getBookId());
        InventoryResponse inventory = (inventoryResponse != null) ? inventoryResponse.getResult() : null;
        if (inventory ==null || inventory.getBookId() == null ){
            throw new AppException(ErrorCode.QUANTITY_EXCEPTION);
        }

        if (inventory.getStock() < cartRequest.getQuantity()) {
            throw new AppException(ErrorCode.QUANTITY_EXCEPTION);
        }

        String key = getCartKey();
        HashOperations<String, String, CartRequest> hashOps = redisTemplate.opsForHash();

        CartRequest existing = hashOps.get(key, cartRequest.getBookId());
        if (existing == null) {
            sendSyncDataEvent(ProductInCart.builder()
                    .bookId(cartRequest.getBookId())
                    .quantity(cartRequest.getQuantity())
                    .instant(Instant.now())
                    .build());
            throw new AppException(ErrorCode.PRODUCT_NOT_FOUND);
        }

        existing.setQuantity(cartRequest.getQuantity());
        hashOps.put(key, cartRequest.getBookId(), existing);
        redisTemplate.expire(key, TTL_IN_DAYS, TimeUnit.DAYS);

        sendSyncDataEvent(ProductInCart.builder()
                .bookId(cartRequest.getBookId())
                .quantity(cartRequest.getQuantity())
                .instant(Instant.now())
                .build());

        return CartResponse.builder()
                .productResponse(product)
                .quantity(cartRequest.getQuantity())
                .build();
    }

    public void removeFromCart(String productId) {
        String key = getCartKey();
        redisTemplate.opsForHash().delete(key, productId);
        sendSyncDataEvent(ProductInCart.builder()
                .bookId(productId)
                .quantity(0)
                .build());
    }

    public void clearCart() {
        String key = getCartKey();
        redisTemplate.delete(key);
        cartRepository.deleteByUserId(SecurityUtils.getUserId());
    }

    public CartResponse fallbackAddProduct() {
        throw new AppException(ErrorCode.PRODUCT_NOT_FOUND);
    }

    private String getCartKey() {
        String userId = SecurityUtils.getUserId();
        if (userId == null || userId.isEmpty()) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        return KEY_PREFIX + userId;
    }


    private void sendSyncDataEvent(ProductInCart productInCart){
        CartSyncDataEvent cartSyncDataEvent  = CartSyncDataEvent.builder()
                .userId(SecurityUtils.getUserId())
                .productInCart(productInCart)
                .build();

        log.info("Sync data cart with mongoDB id {} , data {}" , SecurityUtils.getUserId() , productInCart.getBookId());
        rabbitTemplate.convertAndSend(
                rabbitMQProperties.getExchanges().getCart(),
                rabbitMQProperties.getRoutingKeys().getCartSync(),
                cartSyncDataEvent
        );
    }
}
