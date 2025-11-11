package com.hanguyen.cart_service.service;

import com.hanguyen.cart_service.dto.ProductInCart;
import com.hanguyen.cart_service.dto.event.CartSyncDataEvent;
import com.hanguyen.cart_service.entity.CartDocument;
import com.hanguyen.cart_service.repository.CartRepository;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Builder
@Data
@FieldDefaults(level = AccessLevel.PRIVATE , makeFinal = true)
public class CartSyncConsumer {
    CartRepository cartRepository;

    @RabbitListener(queues = "${spring.rabbitmq.queues.cart-sync}")
    public void handleCartEvent(@Payload CartSyncDataEvent cartSyncDataEvent){
        log.info("Listener get cart event {}" , cartSyncDataEvent.getUserId());

        if (cartSyncDataEvent.getUserId() == null || cartSyncDataEvent.getProductInCart() == null){
            log.info("Cart event invalid");
            return;
        }

        CartDocument cartDocument = cartRepository.findByUserId(cartSyncDataEvent.getUserId())
                .orElseGet(()-> CartDocument.builder()
                        .userId(cartSyncDataEvent.getUserId())
                        .productInCarts(new ArrayList<>())
                        .build());
        List<ProductInCart> productInCarts = cartDocument.getProductInCarts();

        Map<String , ProductInCart> productInCartMap = productInCarts.stream()
                .collect(Collectors.toMap(ProductInCart::getBookId, productInCart -> productInCart));


        ProductInCart productInCart = productInCartMap.get(cartSyncDataEvent.getProductInCart().getBookId());

        if (productInCart != null){
            if(cartSyncDataEvent.getProductInCart().getQuantity()== 0){
                productInCarts.remove(productInCart);
            }
            else {
                productInCart.setQuantity(cartSyncDataEvent.getProductInCart().getQuantity());
            }
        }
        else {
            productInCarts.add(cartSyncDataEvent.getProductInCart());
        }
        cartRepository.save(cartDocument);
    }
}
