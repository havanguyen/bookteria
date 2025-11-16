package com.hanguyen.order_service.service;

import com.hanguyen.order_service.configuration.RabbitMQProperties;
import com.hanguyen.order_service.dto.event.InitiatePaymentCommand;
import com.hanguyen.order_service.dto.event.OrderCompletedEvent;
import com.hanguyen.order_service.dto.event.ReserveInventoryCommand;
import com.hanguyen.order_service.dto.event.RollbackInventoryCommand;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE )
public class SagaProducerService {

    final RabbitTemplate rabbitTemplate;

    final RabbitMQProperties rabbitMQProperties;

    public void sendReserveInventoryCommand(ReserveInventoryCommand cmd) {
        log.info("Sending ReserveInventoryCommand for orderId: {}", cmd.getOrderId());
        rabbitTemplate.convertAndSend(rabbitMQProperties.getExchanges().getInventory(),
                rabbitMQProperties.getRoutingKeys().getInventoryReserve(),
                cmd);
    }

    public void sendOrderCompletedNotification(OrderCompletedEvent event) {
        log.info("Sending OrderCompletedEvent for orderId: {}", event.getOrderId());
        rabbitTemplate.convertAndSend(
                rabbitMQProperties.getExchanges().getNotification(),
                rabbitMQProperties.getRoutingKeys().getNotificationOrderCompleted(), event);
    }

    public void sendInitiatePaymentCommand(InitiatePaymentCommand cmd) {
        log.info("Sending InitiatePaymentCommand for orderId: {}", cmd.getOrderId());
        rabbitTemplate.convertAndSend(rabbitMQProperties.getExchanges().getPayment(),
                rabbitMQProperties.getRoutingKeys().getPaymentInitiate(),
                cmd);
    }

    public void sendRollbackInventoryCommand(RollbackInventoryCommand cmd) {
        log.info("Sending RollbackInventoryCommand for orderId: {}", cmd.getOrderId());
        rabbitTemplate.convertAndSend(rabbitMQProperties.getExchanges().getInventory(),
                rabbitMQProperties.getRoutingKeys().getInventoryRollback(),
                cmd);
    }
}