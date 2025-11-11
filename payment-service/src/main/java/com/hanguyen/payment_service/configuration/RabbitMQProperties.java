package com.hanguyen.payment_service.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;


@ConfigurationProperties(prefix = "spring.rabbitmq")
@Data
public class
RabbitMQProperties {
    private final Exchanges exchanges;
    private final Queues queues;
    private final RoutingKeys routingKeys;

    @Data
    public static class Exchanges {
        private String payment;

        private String order;
    }

    @Data
    public static class Queues {
        private String paymentInitiate;

        private String orderReply;
    }

    @Data
    public static class RoutingKeys {
        private String paymentInitiate;

        private String orderReply;
    }
}

