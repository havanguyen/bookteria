package com.hanguyen.order_service.consumer;

import com.hanguyen.order_service.dto.event.CheckOrderTimeoutEvent;
import com.hanguyen.order_service.dto.event.InitiatePaymentCommand;
import com.hanguyen.order_service.dto.event.OrderItemDto;
import com.hanguyen.order_service.dto.event.RollbackInventoryCommand;
import com.hanguyen.order_service.dto.reply.*;
import com.hanguyen.order_service.entity.OrderItem;
import com.hanguyen.order_service.entity.OrderStatus;
import com.hanguyen.order_service.entity.Orders;
import com.hanguyen.order_service.repository.OrderItemRepository;
import com.hanguyen.order_service.repository.OrderRepository;
import com.hanguyen.order_service.service.OrderService;
import com.hanguyen.order_service.service.SagaProducerService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SagaOrderConsumer {
    OrderRepository orderRepository;
    OrderService orderService;
    SagaProducerService sagaProducerService;

    OrderItemRepository orderItemRepository;

    @RabbitListener(queues = "${spring.rabbitmq.queues.inventory-reserve-reply}")
    public void handleInventoryReserved(@Payload OrderReserverReply reply) {
        Optional<Orders> orders = orderRepository.findById(reply.getOrderId());
        if (orders.isPresent()) {
            InitiatePaymentCommand initiatePaymentCommand = InitiatePaymentCommand.builder()
                    .orderId(reply.getOrderId())
                    .totalAmount(orders.get().getTotalAmount())
                    .ipAddress(orders.get().getIpAddress())
                    .build();
            sagaProducerService.sendInitiatePaymentCommand(initiatePaymentCommand);
            log.info("Send initial Payment command for order id {}", reply.getOrderId());
        }
    }

    @RabbitListener(queues = "${spring.rabbitmq.queues.inventory-oot-reply}")
    public void handleInventoryOutOfStock(@Payload InventoryOutOfStockReply outOfStockReply) {
        log.warn("Received InventoryOutOfStockReply for order: {}", outOfStockReply.getOrderId());

        orderService.updateStatusOrder(outOfStockReply.getOrderId(), OrderStatus.FAILED);

        if (outOfStockReply.getItemsToRollback() != null && !outOfStockReply.getItemsToRollback().isEmpty()) {
            log.info("Sending explicit RollbackInventoryCommand for {} items on order {}.",
                    outOfStockReply.getItemsToRollback().size(), outOfStockReply.getOrderId());

            RollbackInventoryCommand rollbackCmd = RollbackInventoryCommand.builder()
                    .orderId(outOfStockReply.getOrderId())
                    .items(outOfStockReply.getItemsToRollback())
                    .build();
            sagaProducerService.sendRollbackInventoryCommand(rollbackCmd);
        }
    }

    @RabbitListener(queues = "${spring.rabbitmq.queues.inventory-error-reply}")
    public void handleInventoryRollbackError(@Payload InventoryErrorRollBack reply) {
        orderService.updateStatusOrder(reply.getOrderId(), OrderStatus.FAILED);
    }

    @RabbitListener(queues = "${spring.rabbitmq.queues.inventory-rollback-reply}")
    public void handleInventoryRollback(@Payload OrderRollBackReply reply) {
        orderService.updateStatusOrder(reply.getOrderId(), OrderStatus.CANCELLED);
    }

    @RabbitListener(queues = "${spring.rabbitmq.queues.payment-init-reply}")
    public void handlePaymentInitiated(@Payload PaymentInitiatedReply paymentReply) {
        log.info("Received PaymentInitiatedReply for order: {}", paymentReply.getOrderId());
        Optional<Orders> orders = orderRepository.findById(paymentReply.getOrderId());
        if (orders.isPresent()) {
            Orders order = orders.get();
            order.setPaymentUrl(paymentReply.getPaymentUrl());
            orderRepository.save(order);
            log.info("Payment URL saved for order: {}", order.getId());
        } else {
            log.warn("Order not found for saving payment URL: {}", paymentReply.getOrderId());
        }
    }

    @RabbitListener(queues = "${spring.rabbitmq.queues.order-timeout}")
    public void handleOrderTimeout(@Payload CheckOrderTimeoutEvent event) {
        log.warn("Received order timeout check for orderId: {}", event.getOrderId());

        Optional<Orders> orderOpt = orderRepository.findById(event.getOrderId());

        if (orderOpt.isEmpty()) {
            log.error("Order not found for timeout check: {}", event.getOrderId());
            return;
        }

        Orders order = orderOpt.get();

        if (order.getOrderStatus() == OrderStatus.PENDING) {
            log.info("Order {} is still PENDING. Cancelling and rolling back inventory.", order.getId());

            orderService.updateStatusOrder(order.getId(), OrderStatus.CANCELLED);

            List<OrderItem> items = orderItemRepository.findByOrders_Id(order.getId());
            List<OrderItemDto> itemDtos = new ArrayList<>();
            for (OrderItem item : items) {
                itemDtos.add(new OrderItemDto(item.getProductId(), item.getQuantity()));
            }

            RollbackInventoryCommand rollbackCmd = RollbackInventoryCommand.builder()
                    .orderId(order.getId())
                    .items(itemDtos)
                    .build();

            sagaProducerService.sendRollbackInventoryCommand(rollbackCmd);
            log.info("Sent RollbackInventoryCommand for timeout on order: {}", order.getId());

        } else {
            log.info("Order {} has already been processed (Status: {}). Ignoring timeout check.",
                    order.getId(), order.getOrderStatus());
        }
    }
}