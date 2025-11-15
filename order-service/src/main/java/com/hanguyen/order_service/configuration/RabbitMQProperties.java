package com.hanguyen.order_service.configuration;

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
        private String order;

        private String inventory;
        private String payment;
        private String notification;
    }

    @Data
    public static class Queues {
        private String order;
        private String orderReply;

        private String notificationOrderCompleted;
        private String orderTimeout;
        private String orderDelay;
    }

    @Data
    public static class RoutingKeys {
        private String order;
        private String orderReply;

        private String inventoryRollback;
        private String inventoryReserve;
        private String paymentInitiate;
        private String notificationOrderCompleted;
        private String orderTimeout;
        private String orderDelay;
    }
}

