package com.hanguyen.identity.service;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.hanguyen.identity.dto.event.UserEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserEventProducerService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String TOPIC_NAME = "user-registration-topic";
    private static final String TOPIC_USER_UPDATE = "user-updated-topic";
    private static final String TOPIC_USER_DELETE = "user-deleted-topic";

    public void sendUserCreationEvent(UserEvent event) {
        try {
            log.info("Sending user creation event to Kafka: {}", event);
            kafkaTemplate.send(TOPIC_NAME, event.getUserId(), event);
            log.info("Successfully sent event for user ID: {}", event.getUserId());
        } catch (Exception e) {
            log.error(
                    "Error sending user creation event to Kafka for user ID {}: {}",
                    event.getUserId(),
                    e.getMessage(),
                    e);
        }
    }

    public void sendUserUpdateEvent(UserEvent event) {
        try {
            log.info("Sending user update event to Kafka: {}", event);
            kafkaTemplate.send(TOPIC_USER_UPDATE, event.getUserId(), event);
            log.info("Successfully sent update event for user ID: {}", event.getUserId());
        } catch (Exception e) {
            log.error("Error sending user update event for user ID {}: {}", event.getUserId(), e.getMessage(), e);
        }
    }

    public void sendUserDeleteEvent(UserEvent event) {
        try {
            log.info("Sending user delete event to Kafka: {}", event);
            kafkaTemplate.send(TOPIC_USER_DELETE, event.getUserId(), event);
            log.info("Successfully sent delete event for user ID: {}", event.getUserId());
        } catch (Exception e) {
            log.error("Error sending user delete event for user ID {}: {}", event.getUserId(), e.getMessage(), e);
        }
    }
}
