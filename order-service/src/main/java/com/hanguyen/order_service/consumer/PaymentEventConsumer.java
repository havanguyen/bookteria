package com.hanguyen.order_service.consumer;

import com.hanguyen.order_service.dto.ApiResponse;
import com.hanguyen.order_service.dto.event.*;
import com.hanguyen.order_service.dto.response.UserProfileResponse;
import com.hanguyen.order_service.entity.OrderItem;
import com.hanguyen.order_service.entity.Orders;
import com.hanguyen.order_service.entity.OrderStatus;
import com.hanguyen.order_service.repository.OrderItemRepository;
import com.hanguyen.order_service.repository.OrderRepository;
import com.hanguyen.order_service.repository.httpClient.CartClient;
import com.hanguyen.order_service.repository.httpClient.ProfileClient;
import com.hanguyen.order_service.service.OrderService;
import com.hanguyen.order_service.service.SagaProducerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentEventConsumer {

    private final OrderService orderService;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final SagaProducerService sagaProducerService;

    private final CartClient cartClient;
    private final ProfileClient profileClient;

    @KafkaListener(topics = "payment.events", groupId = "order-group")
    public void handlePaymentEvent(Object event) {
        if (event instanceof PaymentSucceededEvent successEvent) {
            log.info("Received PaymentSucceededEvent for order: {}", successEvent.getOrderId());
            Optional<Orders> orderOpt = orderRepository.findById(successEvent.getOrderId());
            if (orderOpt.isEmpty()) {
                log.error("Order not found for cart deletion: {}", successEvent.getOrderId());
                return;
            }
            Orders order = orderOpt.get();
            orderService.updateStatusOrder(successEvent.getOrderId(), OrderStatus.PAID);
            try {
                ApiResponse<String> message = cartClient.deleteCartByUserId(order.getUserId());
                log.info(message.getResult());
            } catch (Exception e) {
                log.error("Failed to delete cart : {}" , e.getMessage());
            }

            try {
                UserProfileResponse profileResponse = profileClient.getProfileByUserId(order.getUserId());

                OrderCompletedEvent notificationEvent = OrderCompletedEvent.builder()
                        .orderId(order.getId())
                        .userId(order.getUserId())
                        .userEmail(profileResponse.getEmail())
                        .customerName(profileResponse.getFirstName() + " " + profileResponse.getLastName())
                        .totalAmount(order.getTotalAmount())
                        .build();

                sagaProducerService.sendOrderCompletedNotification(notificationEvent);
                log.info("Sent order completed notification for order: {}", order.getId());

            } catch (Exception e) {
                log.error("Failed to get user profile or send notification for order {}: {}",
                        order.getId(), e.getMessage());
            }
        } else if (event instanceof PaymentFailedEvent failedEvent) {
            log.warn("Received PaymentFailedEvent for order: {}", failedEvent.getOrderId());

            orderService.updateStatusOrder(failedEvent.getOrderId(), OrderStatus.FAILED);

            Optional<Orders> orderOpt = orderRepository.findById(failedEvent.getOrderId());
            if (orderOpt.isEmpty()) {
                log.error("Order not found for rollback: {}", failedEvent.getOrderId());
                return;
            }

            Orders order = orderOpt.get();
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
            log.info("Sent RollbackInventoryCommand for failed payment on order: {}", order.getId());
        }
    }
}