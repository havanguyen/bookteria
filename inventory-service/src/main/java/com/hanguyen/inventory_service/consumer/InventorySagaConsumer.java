package com.hanguyen.inventory_service.consumer;

import com.hanguyen.inventory_service.dto.request.ReserveInventoryCommand;
import com.hanguyen.inventory_service.dto.request.RollbackInventoryCommand;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class InventorySagaConsumer {

    @RabbitListener(queues = "${spring.rabbitmq.queues.inventory-reserve}")
    public void handleReserveInventory(@Payload ReserveInventoryCommand command) {
        log.info("Nhận được lệnh ReserveInventoryCommand cho Order ID: {}", command.getOrderId());
    }

    @RabbitListener(queues = "${spring.rabbitmq.queues.inventory-rollback}")
    public void handleRollbackInventory(@Payload RollbackInventoryCommand command) {

        log.warn("Nhận được lệnh RollbackInventoryCommand cho Order ID: {}", command.getOrderId());

    }
}