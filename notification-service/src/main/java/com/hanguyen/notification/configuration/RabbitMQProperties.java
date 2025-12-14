package com.hanguyen.notification.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

@ConfigurationProperties(prefix = "spring.rabbitmq")
@Data
public class RabbitMQProperties {
    private final Exchanges exchanges;
    private final Queues queues;
    private final RoutingKeys routingKeys;

    @Data
    public static class Exchanges {
        private String order;
        private String cart;
        private String inventory;
        private String payment;
        private String notification;
    }

    @Data
    public static class Queues {
        private String notificationOrderCompleted;
    }

    @Data
    public static class RoutingKeys {
        private String notificationOrderCompleted;
    }
}
