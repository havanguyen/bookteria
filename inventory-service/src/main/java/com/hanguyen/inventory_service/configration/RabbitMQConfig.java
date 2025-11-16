package com.hanguyen.inventory_service.configration;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class RabbitMQConfig {

    private final RabbitMQProperties rabbitMQProperties;

    @Bean
    public DirectExchange inventoryExchange() {
        return new DirectExchange(rabbitMQProperties.getExchanges().getInventory());
    }

    @Bean
    public Queue reserveInventoryQueue() {
        return new Queue(rabbitMQProperties.getQueues().getInventoryReserve());
    }

    @Bean
    public Queue rollbackInventoryQueue() {
        return new Queue(rabbitMQProperties.getQueues().getInventoryRollback());
    }

    @Bean
    public Queue inventorySyncQueue() {
        return new Queue(rabbitMQProperties.getQueues().getInventorySync());
    }

    @Bean
    public Binding reserveBinding(Queue reserveInventoryQueue, DirectExchange inventoryExchange) {
        return BindingBuilder.bind(reserveInventoryQueue)
                .to(inventoryExchange)
                .with(rabbitMQProperties.getRoutingKeys().getInventoryReserve());
    }

    @Bean
    public Binding rollbackBinding(Queue rollbackInventoryQueue, DirectExchange inventoryExchange) {
        return BindingBuilder.bind(rollbackInventoryQueue)
                .to(inventoryExchange)
                .with(rabbitMQProperties.getRoutingKeys().getInventoryRollback());
    }

    @Bean
    public Binding inventorySyncBinding(Queue inventorySyncQueue, DirectExchange inventoryExchange) {
        return BindingBuilder.bind(inventorySyncQueue)
                .to(inventoryExchange)
                .with(rabbitMQProperties.getRoutingKeys().getInventorySync());
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}