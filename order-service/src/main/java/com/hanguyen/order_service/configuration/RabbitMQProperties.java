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
        private String cart;
        private String inventory;
        private String payment;
        private String notification;
    }

    @Data
    public static class Queues {
        private String inventoryReserve;
        private String inventoryRollback;
        private String paymentInitiate;
        private String inventorySync;
        private String cartSync;
        private String notificationOrderCompleted;
        private String orderTimeout;
        private String orderDelay;
        private String inventoryReserveReply;
        private String inventoryOotReply;
        private String inventoryErrorReply;
        private String inventoryRollbackReply;
        private String paymentInitReply;
        private String paymentSuccessReply;
        private String paymentFailedReply;
    }

    @Data
    public static class RoutingKeys {
        private String inventoryReserve;
        private String inventoryRollback;
        private String paymentInitiate;
        private String inventorySync;
        private String cartSync;
        private String notificationOrderCompleted;
        private String orderTimeout;
        private String orderDelay;
        private String inventoryReserveReply;
        private String inventoryOotReply;
        private String inventoryErrorReply;
        private String inventoryRollbackReply;
        private String paymentInitReply;
        private String paymentSuccessReply;
        private String paymentFailedReply;
    }
}