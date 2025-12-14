package com.hanguyen.notification.configuration;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.AccessLevel;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Configuration
@Data
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RabbitMQConfig {

    final RabbitMQProperties rabbitMQProperties;

    @Bean
    public DirectExchange notificationExchange() {
        return new DirectExchange(rabbitMQProperties.getExchanges().getNotification());
    }

    @Bean
    public Queue notificationQueue() {
        return new Queue(rabbitMQProperties.getQueues().getNotificationOrderCompleted());
    }

    @Bean
    public Binding notificationOrderCompleted(DirectExchange notificationExchange, Queue notificationQueue) {
        return BindingBuilder.bind(notificationQueue)
                .to(notificationExchange)
                .with(rabbitMQProperties.getRoutingKeys().getNotificationOrderCompleted());
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
