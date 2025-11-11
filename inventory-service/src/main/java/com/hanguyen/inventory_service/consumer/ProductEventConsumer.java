package com.hanguyen.inventory_service.consumer;


import com.hanguyen.inventory_service.dto.event.ProductEvent;
import com.hanguyen.inventory_service.dto.event.TypeEvent;
import com.hanguyen.inventory_service.exception.AppException;
import com.hanguyen.inventory_service.repository.InventoryRepository;
import com.hanguyen.inventory_service.service.InventoryService;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
@Builder
@Data
@FieldDefaults(level = AccessLevel.PRIVATE , makeFinal = true)
public class ProductEventConsumer {

    InventoryService inventoryService;

    @KafkaListener(topics = "product.events" , groupId = "inventory-group")
    public void handleProductEvent(@Payload ProductEvent productEvent){
        if(productEvent != null &&
                productEvent.getTypeEvent().matches(TypeEvent.DELETE.getEvent())){
            log.info("Get product event for delete in inventory service for prodcut id {}"
                    , productEvent.getId());

            try {
               inventoryService.deleteStock(productEvent.getId());
            }
            catch (Exception e){
                log.info("Error when delete inventory with product id {} , message error : {}" ,productEvent.getId() , e.getMessage());
            }

        }
    }
}
