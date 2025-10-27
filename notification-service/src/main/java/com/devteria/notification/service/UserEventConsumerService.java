package com.devteria.notification.service;

import com.devteria.notification.constant.TypeEvent;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import com.devteria.notification.dto.event.UserEvent;
import com.devteria.notification.dto.request.Recipient;
import com.devteria.notification.dto.request.SentEmailRequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE , makeFinal = true)
public class UserEventConsumerService {

    EmailService emailService;

    @KafkaListener(topics = "user-registration-topic", groupId = "notification-group")
    public void handleUserCreationEvent(@Payload UserEvent event) {
        log.info("Received user creation event from Kafka: {}", event);

        if (event == null || !StringUtils.hasText(event.getEmail())) {
            log.warn("Received event is null or email is missing. Skipping email sending.");
            return;
        }

        if (!event.getTypeEvent().equals(TypeEvent.CREATE.getEvent())){
            return;
        }

        try {

            SentEmailRequest emailRequest = SentEmailRequest.builder()
                    .recipients(List.of(Recipient.builder()
                            .name(event.getUsername())
                            .email(event.getEmail())
                            .build()))
                    .subject("Welcome to Bookteria!")
                    .htmlContent("<h1>Hello " + event.getUsername() + "!</h1><p>Welcome to bookteria.</p>")
                    .build();

            emailService.sentEmail(emailRequest);
            log.info("Processed welcome email for user ID: {}", event.getUserId());

        } catch (Exception e) {
            log.error("Error processing user creation event for user ID {}: {}", event.getUserId(), e.getMessage(), e);
        }
    }

    @KafkaListener(topics = "user-updated-topic", groupId = "notification-group")
    public void handleUserUpdateEvent(@Payload UserEvent event) {
        log.info("Received user update event from Kafka: {}", event);
        if (event == null || !StringUtils.hasText(event.getEmail())) {
            log.warn("Received update event is null or email is missing. Skipping email sending.");
            return;
        }

        if (!event.getTypeEvent().equals(TypeEvent.UPDATE.getEvent())){
            return;
        }

        try {
            SentEmailRequest emailRequest = SentEmailRequest.builder()
                    .recipients(List.of(Recipient.builder()
                            .name(event.getUsername())
                            .email(event.getEmail())
                            .build()))
                    .subject("Your Bookteria Account Has Been Updated")
                    .htmlContent("<h1>Hello " + event.getUsername() + "!</h1><p>Your account details have been successfully updated.</p>")
                    .build();

            emailService.sentEmail(emailRequest);
            log.info("Sent account update email to {} for user ID: {}", event.getEmail(), event.getUserId());
        } catch (Exception e) {
            log.error("Error processing user update event for user ID {}: {}", event.getUserId(), e.getMessage(), e);
        }
    }

    @KafkaListener(topics = "user-deleted-topic", groupId = "notification-group")
    public void handleUserDeleteEvent(@Payload UserEvent event) {
        log.info("Received user delete event from Kafka: {}", event);
        if (event == null || !StringUtils.hasText(event.getEmail())) {
            log.warn("Received delete event is null or email is missing. Skipping email sending.");
            return;
        }

        if (!event.getTypeEvent().equals(TypeEvent.DELETE.getEvent())){
            return;
        }

        try {
            SentEmailRequest emailRequest = SentEmailRequest.builder()
                    .recipients(List.of(Recipient.builder()
                            .name(event.getUsername())
                            .email(event.getEmail())
                            .build()))
                    .subject("Your Bookteria Account Has Been Deactivated")
                    .htmlContent("<h1>Hello " + event.getUsername() + "!</h1><p>Your account has been deactivated. If you believe this is an error, please contact support.</p>")
                    .build();

            emailService.sentEmail(emailRequest);
            log.info("Sent account deactivation email to {} for user ID: {}", event.getEmail(), event.getUserId());
        } catch (Exception e) {
            log.error("Error processing user delete event for user ID {}: {}", event.getUserId(), e.getMessage(), e);
        }
    }

}