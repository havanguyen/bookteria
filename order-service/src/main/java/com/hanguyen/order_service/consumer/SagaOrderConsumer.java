package com.hanguyen.order_service.consumer;


import com.hanguyen.order_service.dto.event.InitiatePaymentCommand;
import com.hanguyen.order_service.dto.reply.InventoryErrorRollBack;
import com.hanguyen.order_service.dto.reply.InventoryOutOfStockReply;
import com.hanguyen.order_service.dto.reply.OrderReserverReply;
import com.hanguyen.order_service.dto.reply.OrderRollBackReply;
import com.hanguyen.order_service.entity.OrderStatus;
import com.hanguyen.order_service.entity.Orders;
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

import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE , makeFinal = true)
public class SagaOrderConsumer {
    OrderRepository orderRepository;
    OrderService orderService;
    SagaProducerService sagaProducerService;

    @RabbitListener(queues = "spring.rabbitmq.queues.order-reply")
    public void handleOrderReply(@Payload Object reply){
        if(reply instanceof OrderReserverReply){
            Optional<Orders> orders = orderRepository.findById(((OrderReserverReply) reply).getOrderId());
            if(orders.isPresent()){
                InitiatePaymentCommand initiatePaymentCommand = InitiatePaymentCommand.builder()
                        .orderId(((OrderReserverReply) reply).getOrderId())
                        .totalAmount(orders.get().getTotalAmount())
                        .ipAddress("127.0.0.1")
                        .build();
                sagaProducerService.sendInitiatePaymentCommand(initiatePaymentCommand);
                log.info("Send initial Payment command for order id {}" , ((OrderReserverReply) reply).getOrderId());
            }
        }
        else if( reply instanceof InventoryOutOfStockReply || reply instanceof InventoryErrorRollBack){
            assert reply instanceof InventoryOutOfStockReply;
            orderService.updateStatusOrder(((InventoryOutOfStockReply) reply).getOrderId(), OrderStatus.FAILED);
        }
        else if( reply instanceof OrderRollBackReply){
            orderService.updateStatusOrder(((OrderRollBackReply) reply).getOrderId(), OrderStatus.CANCELLED);
        }
    }
}
