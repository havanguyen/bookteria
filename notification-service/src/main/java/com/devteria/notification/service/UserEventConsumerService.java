package com.devteria.notification.service;

import com.devteria.notification.constant.TypeEvent;
import com.devteria.notification.dto.event.UserEvent;
import com.devteria.notification.dto.request.Recipient;
import com.devteria.notification.dto.request.SentEmailRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserEventConsumerService {

    EmailService emailService;
    TemplateEngine templateEngine;

    @KafkaListener(topics = "user-registration-topic", groupId = "notification-group")
    public void handleUserCreationEvent(@Payload UserEvent event) {
        if (event == null || !StringUtils.hasText(event.getEmail()) || !StringUtils.hasText(event.getTypeEvent())) {
            log.warn("Received event is null or email/typeEvent is missing. Skipping email sending.");
            return;
        }
        if (!TypeEvent.CREATE.getEvent().equals(event.getTypeEvent())) {
            log.warn("Received event type {} on create topic. Skipping.", event.getTypeEvent());
            return;
        }

        try {
            Context context = new Context();
            context.setVariable("username", event.getUsername());

            String htmlContent = templateEngine.process("email/welcome", context);

            SentEmailRequest emailRequest = SentEmailRequest.builder()
                    .recipients(List.of(Recipient.builder()
                            .name(event.getUsername())
                            .email(event.getEmail())
                            .build()))
                    .subject("Welcome to Bookteria!")
                    .htmlContent(htmlContent)
                    .build();

            emailService.sentEmail(emailRequest);
            log.info("Processed welcome email for user ID: {}", event.getUserId());
        } catch (Exception e) {
            log.error("Error processing user creation event for user ID {}: {}", event.getUserId(), e.getMessage(), e);
        }
    }

    @KafkaListener(topics = "user-updated-topic", groupId = "notification-group")
    public void handleUserUpdateEvent(@Payload UserEvent event) {
        if (event == null || !StringUtils.hasText(event.getEmail()) || !StringUtils.hasText(event.getTypeEvent())) {
            log.warn("Received update event is null or email/typeEvent is missing. Skipping email sending.");
            return;
        }
        if (!TypeEvent.UPDATE.getEvent().equals(event.getTypeEvent())) {
            log.warn("Received event type {} on update topic. Skipping.", event.getTypeEvent());
            return;
        }

        try {
            Context context = new Context();
            context.setVariable("username", event.getUsername());

            String htmlContent = templateEngine.process("email/account-update", context);

            SentEmailRequest emailRequest = SentEmailRequest.builder()
                    .recipients(List.of(Recipient.builder()
                            .name(event.getUsername())
                            .email(event.getEmail())
                            .build()))
                    .subject("Your Bookteria Account Has Been Updated")
                    .htmlContent(htmlContent)
                    .build();

            emailService.sentEmail(emailRequest);
            log.info("Sent account update email to {} for user ID: {}", event.getEmail(), event.getUserId());
        } catch (Exception e) {
            log.error("Error processing user update event for user ID {}: {}", event.getUserId(), e.getMessage(), e);
        }
    }

    @KafkaListener(topics = "user-deleted-topic", groupId = "notification-group")
    public void handleUserDeleteEvent(@Payload UserEvent event) {
        if (event == null || !StringUtils.hasText(event.getEmail()) || !StringUtils.hasText(event.getTypeEvent())) {
            log.warn("Received delete event is null or email/typeEvent is missing. Skipping email sending.");
            return;
        }
        if (!TypeEvent.DELETE.getEvent().equals(event.getTypeEvent())) {
            log.warn("Received event type {} on delete topic. Skipping.", event.getTypeEvent());
            return;
        }

        try {
            Context context = new Context();
            context.setVariable("username", event.getUsername());

            String htmlContent = templateEngine.process("email/account-delete", context);

            SentEmailRequest emailRequest = SentEmailRequest.builder()
                    .recipients(List.of(Recipient.builder()
                            .name(event.getUsername())
                            .email(event.getEmail())
                            .build()))
                    .subject("Your Bookteria Account Has Been Deactivated")
                    .htmlContent(htmlContent)
                    .build();

            emailService.sentEmail(emailRequest);
            log.info("Sent account deactivation email to {} for user ID: {}", event.getEmail(), event.getUserId());
        } catch (Exception e) {
            log.error("Error processing user delete event for user ID {}: {}", event.getUserId(), e.getMessage(), e);
        }
    }
}