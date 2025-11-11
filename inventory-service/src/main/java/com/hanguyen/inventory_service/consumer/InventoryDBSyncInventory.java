package com.hanguyen.inventory_service.consumer;


import com.hanguyen.inventory_service.dto.event.InventoryUpdateEvent;
import com.hanguyen.inventory_service.entity.InventoryDocument;
import com.hanguyen.inventory_service.repository.InventoryRepository;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
@Builder
@Data
@FieldDefaults(level = AccessLevel.PRIVATE , makeFinal = true)
public class InventoryDBSyncInventory {

    InventoryRepository inventoryRepository;

    @RabbitListener(queues = "${spring.rabbitmq.queues.inventory-sync}")
    public void handleInventoryUpdate(@Payload InventoryUpdateEvent inventoryUpdateEvent){
        log.info("Get update inventory event with book have id {} " , inventoryUpdateEvent.getBookId());

        InventoryDocument inventoryDocument = inventoryRepository.findByBookId(inventoryUpdateEvent.getBookId())
                .orElseGet(() -> InventoryDocument.builder()
                        .bookId(inventoryUpdateEvent.getBookId())
                        .build());
        inventoryDocument.setStock(inventoryUpdateEvent.getNewStock());

        inventoryRepository.save(inventoryDocument);
    }
}
