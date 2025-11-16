package com.hanguyen.payment_service.service;

import com.hanguyen.payment_service.configuration.RabbitMQProperties;
import com.hanguyen.payment_service.dto.event.CheckOrderTimeoutEvent;
import com.hanguyen.payment_service.dto.reponse.PaymentInitiatedReply;
import com.hanguyen.payment_service.dto.request.InitiatePaymentCommand;
import com.hanguyen.payment_service.entity.PaymentStatus;
import com.hanguyen.payment_service.entity.PaymentTransactions;
import com.hanguyen.payment_service.repository.PaymentRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PaymentInitConsumer {
    PaymentRepository paymentRepository;

    VNPayService vnPayService;
    RabbitMQProperties rabbitMQProperties;
    RabbitTemplate rabbitTemplate;

    @RabbitListener(queues = "${spring.rabbitmq.queues.payment-initiate}")
    public void handleInventoryUpdate(@Payload InitiatePaymentCommand initiatePaymentCommand) {
        log.info("Get init payment command event with order id {} ", initiatePaymentCommand.getOrderId());

        try {
            String vnp_TxnRef = UUID.randomUUID().toString();
            String paymentUrl = vnPayService.createPaymentUrl(
                    initiatePaymentCommand.getOrderId(),
                    initiatePaymentCommand.getTotalAmount(),
                    vnp_TxnRef,
                    initiatePaymentCommand.getIpAddress()
            );

            log.info("Create url {}", paymentUrl);

            PaymentTransactions transaction = PaymentTransactions.builder()
                    .orderId(initiatePaymentCommand.getOrderId())
                    .externalTransactionId(vnp_TxnRef)
                    .paymentMethod("VNPay")
                    .amount(initiatePaymentCommand.getTotalAmount())
                    .paymentStatus(PaymentStatus.PENDING)
                    .build();
            paymentRepository.save(transaction);

            PaymentInitiatedReply reply = PaymentInitiatedReply.builder()
                    .orderId(initiatePaymentCommand.getOrderId())
                    .paymentUrl(paymentUrl)
                    .message("Payment URL created.")
                    .build();

            rabbitTemplate.convertAndSend(
                    rabbitMQProperties.getExchanges().getOrder(),
                    rabbitMQProperties.getRoutingKeys().getPaymentInitReply(),
                    reply
            );
            log.info("Sent payment URL reply for orderId: {}", initiatePaymentCommand.getOrderId());

            CheckOrderTimeoutEvent checkOrderTimeoutEvent = CheckOrderTimeoutEvent.builder()
                    .orderId(initiatePaymentCommand.getOrderId())
                    .build();

            rabbitTemplate.convertAndSend(
                    rabbitMQProperties.getExchanges().getOrder(),
                    rabbitMQProperties.getRoutingKeys().getOrderDelay(),
                    checkOrderTimeoutEvent
            );
            log.info("Sent order timeout check message for orderId: {}", initiatePaymentCommand.getOrderId());

        } catch (Exception e) {
            log.error("Error creating payment URL for orderId: {}", initiatePaymentCommand.getOrderId(), e);

            PaymentInitiatedReply reply = PaymentInitiatedReply.builder()
                    .orderId(initiatePaymentCommand.getOrderId())
                    .paymentUrl(null)
                    .message("Failed to create payment URL: " + e.getMessage())
                    .build();

            rabbitTemplate.convertAndSend(
                    rabbitMQProperties.getExchanges().getOrder(),
                    rabbitMQProperties.getRoutingKeys().getPaymentInitReply(),
                    reply
            );
        }
    }
}