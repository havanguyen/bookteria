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
    public static final long TTL = 900000L;

    @Bean
    public DirectExchange orderExchange() {
        return new DirectExchange(rabbitMQProperties.getExchanges().getOrder());
    }

    @Bean
    public Queue orderQueue() {
        return new Queue(rabbitMQProperties.getQueues().getOrder());
    }

    @Bean
    public Queue orderReplyQueue() {
        return new Queue(rabbitMQProperties.getQueues().getOrderReply());
    }


    @Bean
    public Queue orderDelayQueue(){
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
    public Binding orderBinding(DirectExchange directExchange, Queue orderQueue) {
        return BindingBuilder.bind(orderQueue)
                .to(directExchange)
                .with(rabbitMQProperties.getRoutingKeys().getOrder());
    }

    @Bean
    public Binding orderReplyBinding(DirectExchange directExchange, Queue orderReplyQueue) {
        return BindingBuilder.bind(orderReplyQueue)
                .to(directExchange)
                .with(rabbitMQProperties.getRoutingKeys().getOrderReply());
    }

    @Bean
    public Binding orderTimeoutBinding(DirectExchange directExchange, Queue orderTimeoutQueue) {
        return BindingBuilder.bind(orderTimeoutQueue)
                .to(directExchange)
                .with(rabbitMQProperties.getRoutingKeys().getOrderTimeout());
    }

    @Bean
    public Binding orderDelayBinding(DirectExchange directExchange, Queue orderDelayQueue) {
        return BindingBuilder.bind(orderDelayQueue)
                .to(directExchange)
                .with(rabbitMQProperties.getRoutingKeys().getOrderDelay());
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}