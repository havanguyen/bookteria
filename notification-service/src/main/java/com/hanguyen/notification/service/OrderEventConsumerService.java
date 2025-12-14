package com.hanguyen.notification.service;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.hanguyen.notification.dto.event.OrderCompletedEvent;
import com.hanguyen.notification.dto.request.Recipient;
import com.hanguyen.notification.dto.request.SentEmailRequest;
import com.hanguyen.notification.dto.response.EmailResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderEventConsumerService {

    private final EmailService emailService;
    TemplateEngine templateEngine;

    @RabbitListener(queues = "${spring.rabbitmq.queues.notification-order-completed}")
    public void handleOrderCompleted(OrderCompletedEvent event) {
        log.info("Sending order confirmation email to {}", event.getUserEmail());

        Context context = new Context();
        context.setVariable("customerName", event.getCustomerName());
        context.setVariable("orderId", event.getOrderId());

        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        String formattedTotal = currencyFormatter.format(event.getTotalAmount());
        context.setVariable("totalAmount", formattedTotal);

        String htmlContent = templateEngine.process("email/order-confirmation", context);

        SentEmailRequest emailRequest = SentEmailRequest.builder()
                .recipients(List.of(Recipient.builder()
                        .name(event.getCustomerName())
                        .email(event.getUserEmail())
                        .build()))
                .subject("Bookteria - Xác nhận đơn hàng #" + event.getOrderId())
                .htmlContent(htmlContent)
                .build();

        try {
            EmailResponse emailResponse = emailService.sentEmail(emailRequest);
            log.info("Order confirmation email sent successfully, messageId: {}", emailResponse.getMessageId());
        } catch (Exception e) {
            log.error("Error sending order confirmation email", e);
        }
    }
}
