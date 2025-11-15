package com.hanguyen.payment_service.configuration;


import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE , makeFinal = true)
public class RabbitMQConfig {
    RabbitMQProperties rabbitMQProperties;

    @Bean
    public DirectExchange directExchange(){
        return new DirectExchange(rabbitMQProperties.getExchanges().getPayment());
    }

    @Bean
    public Queue queueInitPayment(){
        return new Queue(rabbitMQProperties.getQueues().getPaymentInitiate());
    }

    @Bean
    public Queue queueReplyPayment(){
        return new Queue(rabbitMQProperties.getQueues().getPaymentReply());
    }

    @Bean
    public Binding paymentInitBiding(Queue queueInitPayment , DirectExchange directExchange){
        return BindingBuilder.bind(queueInitPayment)
                .to(directExchange)
                .with(rabbitMQProperties.getRoutingKeys().getPaymentInitiate());
    }

    @Bean
    public Binding paymentReplyBiding(Queue queueReplyPayment , DirectExchange directExchange){
        return BindingBuilder.bind(queueReplyPayment)
                .to(directExchange)
                .with(rabbitMQProperties.getRoutingKeys().getPaymentReply());
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

}
