package com.hanguyen.inventory_service.consumer;

import com.hanguyen.inventory_service.configration.RabbitMQProperties;
import com.hanguyen.inventory_service.dto.reply.InventoryErrorRollBack;
import com.hanguyen.inventory_service.dto.reply.InventoryOutOfStockReply;
import com.hanguyen.inventory_service.dto.reply.OrderReserverReply;
import com.hanguyen.inventory_service.dto.reply.OrderRollBackReply;
import com.hanguyen.inventory_service.dto.request.OrderItemDto;
import com.hanguyen.inventory_service.dto.request.ReserveInventoryCommand;
import com.hanguyen.inventory_service.dto.request.RollbackInventoryCommand;
import com.hanguyen.inventory_service.exception.AppException;
import com.hanguyen.inventory_service.exception.ErrorCode;
import com.hanguyen.inventory_service.service.InventoryService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE , makeFinal = true)
public class InventorySagaConsumer {

    InventoryService inventoryService;

    RabbitMQProperties rabbitMQProperties;

    RabbitTemplate rabbitTemplate;

    StringRedisTemplate stringRedisTemplate;

    @RabbitListener(queues = "${spring.rabbitmq.queues.inventory-reserve}")
    public void handleReserveInventory(@Payload ReserveInventoryCommand command) {

        String orderId = command.getOrderId();

        String idempotencyKey = "inventory_deduct_lock:" + orderId;

        Boolean isFirstProcess = stringRedisTemplate.opsForValue().setIfAbsent(idempotencyKey , "PROCESSING" , 1 , TimeUnit.HOURS);

        if (Boolean.FALSE.equals(isFirstProcess)){
            log.warn("Phát hiện trùng lặp! Đơn hàng {} đang được xử lý hoặc đã xong. Bỏ qua.", orderId);
            return;
        }

        try {
            log.info("Nhận được lệnh ReserveInventoryCommand cho Order ID: {}", orderId);
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
                            rabbitMQProperties.getRoutingKeys().getInventoryOotReply(),
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
                        rabbitMQProperties.getRoutingKeys().getInventoryReserveReply(),
                        reply
                );
            }
        } catch (Exception e) {
            log.info("Error when revert  {}" , e.getMessage());
            stringRedisTemplate.delete(idempotencyKey);
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }

    }

    @RabbitListener(queues = "${spring.rabbitmq.queues.inventory-rollback}")
    public void handleRollbackInventory(@Payload RollbackInventoryCommand command) {
        log.warn("Nhận được lệnh RollbackInventoryCommand cho Order ID: {}", command.getOrderId());

        String orderId = command.getOrderId();

        String idempotencyKey = "inventory_rollback:" + orderId;

        Boolean isFirstProcess = stringRedisTemplate.opsForValue().setIfAbsent(idempotencyKey , "PROCESSING" , 1 , TimeUnit.HOURS);

        if (Boolean.FALSE.equals(isFirstProcess)){
            log.warn("Phát hiện rollback trùng lặp! Đơn hàng {} đang được xử lý hoặc đã xong. Bỏ qua.", orderId);
            return;
        }

        try {
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
                            rabbitMQProperties.getRoutingKeys().getInventoryErrorReply(),
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
                        rabbitMQProperties.getRoutingKeys().getInventoryRollbackReply(),
                        reply
                );
            }
        } catch (Exception e){
            log.info("Error when rollback {}" , e.getMessage());
            stringRedisTemplate.delete(idempotencyKey);
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }
}