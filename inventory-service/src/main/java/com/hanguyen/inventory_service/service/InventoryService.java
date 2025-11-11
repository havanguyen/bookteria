package com.hanguyen.inventory_service.service;

import com.hanguyen.inventory_service.configration.RabbitMQConfig;
import com.hanguyen.inventory_service.configration.RabbitMQProperties;
import com.hanguyen.inventory_service.consumer.InventoryDBSyncInventory;
import com.hanguyen.inventory_service.dto.ApiResponse;
import com.hanguyen.inventory_service.dto.event.InventoryUpdateEvent;
import com.hanguyen.inventory_service.dto.reponse.ProductResponse;
import com.hanguyen.inventory_service.dto.request.InventoryRequest;
import com.hanguyen.inventory_service.entity.InventoryDocument;
import com.hanguyen.inventory_service.exception.AppException;
import com.hanguyen.inventory_service.exception.ErrorCode;
import com.hanguyen.inventory_service.repository.InventoryRepository;
import com.hanguyen.inventory_service.repository.httpClient.ProductClient;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE , makeFinal = true)
public class InventoryService {

    static String KEY_PREFIX = "inventory:stock:";

    StringRedisTemplate stringRedisTemplate;
    RedisScript<Long> decreaseStockScript;

    InventoryRepository inventoryRepository;
    RabbitTemplate rabbitTemplate;

    ProductClient productClient;

    RabbitMQProperties rabbitMQProperties;


    public int getStock(String bookId) {
        String stock = stringRedisTemplate.opsForValue()
                .get(KEY_PREFIX + bookId);
        if (stock != null) {
            try {
                return Integer.parseInt(stock);
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        log.warn("Cache miss for bookId: {}. Fetching from MongoDB.", bookId);

        Optional<InventoryDocument> inventoryDocument = inventoryRepository.findByBookId(bookId);
        if(inventoryDocument.isPresent()){
            int stockQuantity = inventoryDocument.get().getStock();

            stringRedisTemplate.opsForValue().set(KEY_PREFIX + bookId ,
                    String.valueOf(stockQuantity));
            return stockQuantity;
        }
        else {
            return 0;
        }
    }

    @Transactional
    public int setStock(InventoryRequest request) {

        try {
            ApiResponse<ProductResponse> productResponseApiResponse =
                    productClient.getProductById(request.getBookId());

            if(productResponseApiResponse == null ||
                    productResponseApiResponse.getResult() == null ||
                    productResponseApiResponse.getResult().getId() == null
            ){
                throw new AppException(ErrorCode.PRODUCT_NOT_FOUND);
            }
        }
        catch (Exception e){
            log.info("Exception when get product with id {} , {}" , request.getBookId() , e.getMessage());
            throw new AppException(ErrorCode.PRODUCT_NOT_FOUND);
        }


        if (request.getQuantity() < 0) {
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
        try {
            stringRedisTemplate.opsForValue().set(KEY_PREFIX + request.getBookId(),
                    String.valueOf(request.getQuantity()));
        }
        catch (Exception e){
            log.info("Error when add data to redis with key {} , value {}" , KEY_PREFIX + request.getBookId() , request.getQuantity());
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
        sendSyncEvent(request.getBookId() , request.getQuantity());
        return request.getQuantity();
    }

    @Transactional
    public boolean decreaseStock(String bookId, int quantity) {
        if (quantity <= 0) {
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }

        long result = stringRedisTemplate.execute(
                decreaseStockScript,
                Collections.singletonList(KEY_PREFIX + bookId),
                String.valueOf(quantity)
        );
        if( result == 1){
            String stock = stringRedisTemplate.opsForValue().get(KEY_PREFIX + bookId);
            int stockInt = 0 ;
            if(stock != null){
                try {
                    stockInt = Integer.parseInt(stock);
                }
                catch (Exception e){
                    log.info("Error when parse {} to Integer" , stock);
                }
            }
            sendSyncEvent(bookId , stockInt);
        }
        return result == 1;
    }

    @Transactional
    public void deleteStock(String bookId){

        if(bookId == null || bookId.isEmpty()){
            return;
        }
        Optional<InventoryDocument> inventoryDocument = inventoryRepository.findByBookId(bookId);

        stringRedisTemplate.delete(KEY_PREFIX + bookId);
        if(inventoryDocument.isPresent()){
            inventoryRepository.deleteByBookId(bookId);
        }
    }



    private void sendSyncEvent(String bookId , int quantity){
        InventoryUpdateEvent inventoryUpdateEvent = InventoryUpdateEvent.builder()
                .bookId(bookId)
                .newStock(quantity)
                .build();

        log.info("Sync data bookId : {} , stock : {}" , bookId , quantity);
        rabbitTemplate.convertAndSend(
                rabbitMQProperties.getExchanges().getInventory() ,
                rabbitMQProperties.getRoutingKeys().getInventorySync(),
                inventoryUpdateEvent
        );
    }
}