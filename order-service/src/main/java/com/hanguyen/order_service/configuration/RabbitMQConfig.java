package com.hanguyen.order_service.configuration;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Data
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RabbitMQConfig {

    final RabbitMQProperties rabbitMQProperties;

    @Bean
    public DirectExchange orderExchange() {
        return new DirectExchange(rabbitMQProperties.getExchanges().getOrder());
    }

    @Bean
    public Queue inventoryReserveReplyQueue() {
        return new Queue(rabbitMQProperties.getQueues().getInventoryReserveReply());
    }

    @Bean
    public Queue inventoryOotReplyQueue() {
        return new Queue(rabbitMQProperties.getQueues().getInventoryOotReply());
    }

    @Bean
    public Queue inventoryErrorReplyQueue() {
        return new Queue(rabbitMQProperties.getQueues().getInventoryErrorReply());
    }

    @Bean
    public Queue inventoryRollbackReplyQueue() {
        return new Queue(rabbitMQProperties.getQueues().getInventoryRollbackReply());
    }

    @Bean
    public Queue paymentInitReplyQueue() {
        return new Queue(rabbitMQProperties.getQueues().getPaymentInitReply());
    }

    @Bean
    public Queue paymentSuccessReplyQueue() {
        return new Queue(rabbitMQProperties.getQueues().getPaymentSuccessReply());
    }

    @Bean
    public Queue paymentFailedReplyQueue() {
        return new Queue(rabbitMQProperties.getQueues().getPaymentFailedReply());
    }

    @Bean
    public Queue orderDelayQueue() {
        return QueueBuilder.durable(rabbitMQProperties.getQueues().getOrderDelay())
                .withArgument("x-message-ttl", 15 * 60 * 1000)
                .withArgument("x-dead-letter-exchange", rabbitMQProperties.getExchanges().getOrder())
                .withArgument("x-dead-letter-routing-key", rabbitMQProperties.getRoutingKeys().getOrderTimeout())
                .build();
    }

    @Bean
    public Queue orderTimeoutQueue() {
        return QueueBuilder.durable(rabbitMQProperties.getQueues().getOrderTimeout()).build();
    }

    @Bean
    public Binding inventoryReserveReplyBinding(DirectExchange orderExchange, Queue inventoryReserveReplyQueue) {
        return BindingBuilder.bind(inventoryReserveReplyQueue)
                .to(orderExchange)
                .with(rabbitMQProperties.getRoutingKeys().getInventoryReserveReply());
    }

    @Bean
    public Binding inventoryOotReplyBinding(DirectExchange orderExchange, Queue inventoryOotReplyQueue) {
        return BindingBuilder.bind(inventoryOotReplyQueue)
                .to(orderExchange)
                .with(rabbitMQProperties.getRoutingKeys().getInventoryOotReply());
    }

    @Bean
    public Binding inventoryErrorReplyBinding(DirectExchange orderExchange, Queue inventoryErrorReplyQueue) {
        return BindingBuilder.bind(inventoryErrorReplyQueue)
                .to(orderExchange)
                .with(rabbitMQProperties.getRoutingKeys().getInventoryErrorReply());
    }

    @Bean
    public Binding inventoryRollbackReplyBinding(DirectExchange orderExchange, Queue inventoryRollbackReplyQueue) {
        return BindingBuilder.bind(inventoryRollbackReplyQueue)
                .to(orderExchange)
                .with(rabbitMQProperties.getRoutingKeys().getInventoryRollbackReply());
    }

    @Bean
    public Binding paymentInitReplyBinding(DirectExchange orderExchange, Queue paymentInitReplyQueue) {
        return BindingBuilder.bind(paymentInitReplyQueue)
                .to(orderExchange)
                .with(rabbitMQProperties.getRoutingKeys().getPaymentInitReply());
    }

    @Bean
    public Binding paymentSuccessReplyBinding(DirectExchange orderExchange, Queue paymentSuccessReplyQueue) {
        return BindingBuilder.bind(paymentSuccessReplyQueue)
                .to(orderExchange)
                .with(rabbitMQProperties.getRoutingKeys().getPaymentSuccessReply());
    }

    @Bean
    public Binding paymentFailedReplyBinding(DirectExchange orderExchange, Queue paymentFailedReplyQueue) {
        return BindingBuilder.bind(paymentFailedReplyQueue)
                .to(orderExchange)
                .with(rabbitMQProperties.getRoutingKeys().getPaymentFailedReply());
    }

    @Bean
    public Binding orderTimeoutBinding(DirectExchange orderExchange, Queue orderTimeoutQueue) {
        return BindingBuilder.bind(orderTimeoutQueue)
                .to(orderExchange)
                .with(rabbitMQProperties.getRoutingKeys().getOrderTimeout());
    }

    @Bean
    public Binding orderDelayBinding(DirectExchange orderExchange, Queue orderDelayQueue) {
        return BindingBuilder.bind(orderDelayQueue)
                .to(orderExchange)
                .with(rabbitMQProperties.getRoutingKeys().getOrderDelay());
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}