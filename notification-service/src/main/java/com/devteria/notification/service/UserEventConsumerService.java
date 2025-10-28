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
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserEventConsumerService {

    EmailService emailService;

    @KafkaListener(topics = "user-registration-topic", groupId = "notification-group")
    public void handleUserCreationEvent(@Payload UserEvent event) {
        log.info("Received user creation event from Kafka: {}", event);

        if (event == null
                || !StringUtils.hasText(event.getEmail())
                || !StringUtils.hasText(event.getTypeEvent())) {
            log.warn("Received event is null or email/typeEvent is missing. Skipping email sending.");
            return;
        }

        if (!TypeEvent.CREATE.getEvent().equals(event.getTypeEvent())) {
            log.warn("Received event type {} on create topic. Skipping.", event.getTypeEvent());
            return;
        }

        try {
            String htmlContent = String.format("""
                    <!DOCTYPE html>
                    <html>
                    <head>
                      <meta charset="UTF-8">
                      <style>
                        body { font-family: 'Segoe UI', Arial, sans-serif; background-color: #f6f9fc; margin: 0; padding: 0; }
                        .container { max-width: 600px; margin: 40px auto; background: #ffffff; border-radius: 10px; padding: 30px; box-shadow: 0 4px 10px rgba(0,0,0,0.05); }
                        .header { text-align: center; }
                        .logo { font-size: 28px; color: #0077cc; font-weight: bold; }
                        .content { margin-top: 20px; line-height: 1.6; color: #333; }
                        .button { display: inline-block; margin-top: 20px; padding: 12px 24px; background-color: #0077cc; color: #fff; border-radius: 6px; text-decoration: none; }
                        .footer { margin-top: 30px; text-align: center; font-size: 12px; color: #999; }
                      </style>
                    </head>
                    <body>
                      <div class="container">
                        <div class="header">
                          <div class="logo">📚 Bookteria</div>
                        </div>
                        <div class="content">
                          <h2>Welcome, %s!</h2>
                          <p>Thank you for joining <strong>Bookteria</strong> — your new home for discovering, sharing, and managing your favorite books.</p>
                          <p>We're excited to have you on board. Start exploring now!</p>
                          <a href="https://bookteria.dev/login" class="button">Go to Bookteria</a>
                        </div>
                        <div class="footer">
                          © 2025 Bookteria Inc. All rights reserved.
                        </div>
                      </div>
                    </body>
                    </html>
                    """, event.getUsername());

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
        log.info("Received user update event from Kafka: {}", event);

        if (event == null
                || !StringUtils.hasText(event.getEmail())
                || !StringUtils.hasText(event.getTypeEvent())) {
            log.warn("Received update event is null or email/typeEvent is missing. Skipping email sending.");
            return;
        }

        if (!TypeEvent.UPDATE.getEvent().equals(event.getTypeEvent())) {
            log.warn("Received event type {} on update topic. Skipping.", event.getTypeEvent());
            return;
        }

        try {
            String htmlContent = String.format("""
                    <!DOCTYPE html>
                    <html>
                    <head>
                      <meta charset="UTF-8">
                      <style>
                        body { font-family: 'Segoe UI', Arial, sans-serif; background-color: #f4f7fb; margin: 0; padding: 0; }
                        .container { max-width: 600px; margin: 40px auto; background: #fff; border-radius: 10px; padding: 30px; box-shadow: 0 4px 10px rgba(0,0,0,0.05); }
                        .header { text-align: center; color: #0077cc; font-weight: bold; font-size: 26px; }
                        .content { margin-top: 20px; line-height: 1.6; color: #333; }
                        .highlight { color: #0077cc; font-weight: 600; }
                        .footer { margin-top: 30px; text-align: center; font-size: 12px; color: #999; }
                      </style>
                    </head>
                    <body>
                      <div class="container">
                        <div class="header">Bookteria Update Notice</div>
                        <div class="content">
                          <p>Hello <strong>%s</strong>,</p>
                          <p>We wanted to let you know that your <span class="highlight">Bookteria</span> account information has been successfully updated.</p>
                          <p>If this wasn’t you, please contact our support team immediately.</p>
                          <p>Thank you for staying with us!</p>
                        </div>
                        <div class="footer">
                          © 2025 Bookteria. Need help? <a href="mailto:nguyenjavax@gmail.com">Contact Support</a>
                        </div>
                      </div>
                    </body>
                    </html>
                    """, event.getUsername());

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
        log.info("Received user delete event from Kafka: {}", event);

        if (event == null
                || !StringUtils.hasText(event.getEmail())
                || !StringUtils.hasText(event.getTypeEvent())) {
            log.warn("Received delete event is null or email/typeEvent is missing. Skipping email sending.");
            return;
        }

        if (!TypeEvent.DELETE.getEvent().equals(event.getTypeEvent())) {
            log.warn("Received event type {} on delete topic. Skipping.", event.getTypeEvent());
            return;
        }

        try {
            String htmlContent = String.format("""
                    <!DOCTYPE html>
                    <html>
                    <head>
                      <meta charset="UTF-8">
                      <style>
                        body { font-family: 'Segoe UI', Arial, sans-serif; background-color: #f8f9fa; margin: 0; padding: 0; }
                        .container { max-width: 600px; margin: 40px auto; background: #fff; border-radius: 10px; padding: 30px; box-shadow: 0 4px 10px rgba(0,0,0,0.05); }
                        .header { text-align: center; color: #d9534f; font-weight: bold; font-size: 26px; }
                        .content { margin-top: 20px; line-height: 1.6; color: #333; }
                        .button { display: inline-block; margin-top: 20px; padding: 12px 24px; background-color: #0077cc; color: #fff; border-radius: 6px; text-decoration: none; }
                        .footer { margin-top: 30px; text-align: center; font-size: 12px; color: #999; }
                      </style>
                    </head>
                    <body>
                      <div class="container">
                        <div class="header">Account Deactivation Notice</div>
                        <div class="content">
                          <p>Dear <strong>%s</strong>,</p>
                          <p>Your <strong>Bookteria</strong> account has been deactivated as per your request or administrative decision.</p>
                          <p>If this was not intended, please contact our support team immediately to restore your access.</p>
                          <a href="mailto:nguyenjavax@gmail.com" class="button">Contact Support</a>
                        </div>
                        <div class="footer">
                          © 2025 Bookteria. All rights reserved.
                        </div>
                      </div>
                    </body>
                    </html>
                    """, event.getUsername());

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
