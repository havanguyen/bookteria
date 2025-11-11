package com.hanguyen.cart_service.configration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;


@ConfigurationProperties(prefix = "spring.rabbitmq")
@Data
public class RabbitMQProperties {
    private final Exchanges exchanges;
    private final Queues queues;
    private final RoutingKeys routingKeys;

    @Data
    public static class Exchanges {
        private String cart;
    }

    @Data
    public static class Queues {
        private String cartSync;
    }

    @Data
    public static class RoutingKeys {
        private String cartSync;
    }
}

