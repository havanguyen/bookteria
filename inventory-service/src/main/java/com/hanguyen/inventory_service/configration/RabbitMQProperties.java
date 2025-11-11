package com.hanguyen.inventory_service.configration;

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
        private String inventory;
    }

    @Data
    public static class Queues {
        private String inventoryReserve;
        private String inventoryRollback;
        private String inventorySync;
    }

    @Data
    public static class RoutingKeys {
        private String inventoryReserve;
        private String inventoryRollback;
        private String inventorySync;
    }
}

