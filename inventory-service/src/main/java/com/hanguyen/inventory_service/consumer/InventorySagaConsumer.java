package com.hanguyen.inventory_service.consumer;

import com.hanguyen.inventory_service.configration.RabbitMQProperties;
import com.hanguyen.inventory_service.dto.reply.InventoryErrorRollBack;
import com.hanguyen.inventory_service.dto.reply.InventoryOutOfStockReply;
import com.hanguyen.inventory_service.dto.reply.OrderReserverReply;
import com.hanguyen.inventory_service.dto.reply.OrderRollBackReply;
import com.hanguyen.inventory_service.dto.request.OrderItemDto;
import com.hanguyen.inventory_service.dto.request.ReserveInventoryCommand;
import com.hanguyen.inventory_service.dto.request.RollbackInventoryCommand;
import com.hanguyen.inventory_service.service.InventoryService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE , makeFinal = true)
public class InventorySagaConsumer {

    InventoryService inventoryService;

    RabbitMQProperties rabbitMQProperties;

    RabbitTemplate rabbitTemplate;

    @RabbitListener(queues = "${spring.rabbitmq.queues.inventory-reserve}")
    public void handleReserveInventory(@Payload ReserveInventoryCommand command) {
        log.info("Nhận được lệnh ReserveInventoryCommand cho Order ID: {}", command.getOrderId());
        List<OrderItemDto> successfullyDecreased = new ArrayList<>();
        boolean success = false;

        for(OrderItemDto orderItemDto : command.getItems()){
          success =  inventoryService.decreaseStock(orderItemDto.getBookId() , orderItemDto.getQuantity());
          if (!success){
              InventoryOutOfStockReply reply = InventoryOutOfStockReply.builder()
                      .orderId(command.getOrderId())
                      .quantity(orderItemDto.getQuantity())
                      .bookId(orderItemDto.getBookId())
                      .itemsToRollback(successfullyDecreased)
                      .message(String.format("Book has id %s out of stock , quantity only %d left",
                              orderItemDto.getBookId() , orderItemDto.getQuantity()))
                      .build();
                rabbitTemplate.convertAndSend(
                        rabbitMQProperties.getExchanges().getOrder(),
                        rabbitMQProperties.getRoutingKeys().getOrderReply(),
                        reply
                );
                return;
          }
          else {
              successfullyDecreased.add(orderItemDto);
          }
        }
        if (success) {
            OrderReserverReply reply = OrderReserverReply.builder()
                    .orderId(command.getOrderId())
                    .message(String.format("Revert all book in order has id %s", command.getOrderId()))
                    .build();
            rabbitTemplate.convertAndSend(
                    rabbitMQProperties.getExchanges().getOrder(),
                    rabbitMQProperties.getRoutingKeys().getOrderReply(),
                    reply
            );
        }
    }

    @RabbitListener(queues = "${spring.rabbitmq.queues.inventory-rollback}")
    public void handleRollbackInventory(@Payload RollbackInventoryCommand command) {
        log.warn("Nhận được lệnh RollbackInventoryCommand cho Order ID: {}", command.getOrderId());
        int checkErr = 0;
        for(OrderItemDto orderItemDto : command.getItems()){
            boolean success =  inventoryService.increaseStock(orderItemDto.getBookId() , orderItemDto.getQuantity());
            if (!success){
                InventoryErrorRollBack reply = InventoryErrorRollBack.builder()
                        .orderId(command.getOrderId())
                        .bookId(orderItemDto.getBookId())
                        .message(String.format("Book has id %s out found",
                                orderItemDto.getBookId()))
                        .build();
                rabbitTemplate.convertAndSend(
                        rabbitMQProperties.getExchanges().getOrder(),
                        rabbitMQProperties.getRoutingKeys().getOrderReply(),
                        reply
                );
                checkErr = 1;
            }
        }
        if (checkErr == 0) {
            OrderRollBackReply reply = OrderRollBackReply.builder()
                    .orderId(command.getOrderId())
                    .message(String.format("Revert all book in order has id %s", command.getOrderId()))
                    .build();
            rabbitTemplate.convertAndSend(
                    rabbitMQProperties.getExchanges().getOrder(),
                    rabbitMQProperties.getRoutingKeys().getOrderReply(),
                    reply
            );
        }
    }
}