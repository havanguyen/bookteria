package com.hanguyen.notification.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;


@ConfigurationProperties(prefix = "spring.rabbitmq")
@Data
public class RabbitMQProperties {
    private final Exchanges exchanges;
    private final Queues queues;
    private final RoutingKeys routingKeys;

    @Data
    public static class Exchanges {
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

