package com.hanguyen.product_service.service;

import com.hanguyen.product_service.dto.event.ProductEvent;
import com.hanguyen.product_service.dto.event.TypeEvent;
import com.hanguyen.product_service.entity.Product;
import com.hanguyen.product_service.mapper.ProductMapper;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE , makeFinal = true)
public class ProductEventProducerService {
    KafkaTemplate<String, Object> kafkaTemplate;
    ProductMapper productMapper;

    public void sendProductEvent(Product product , TypeEvent typeEvent) {
        ProductEvent event = productMapper.toProductEvent(product);

        event.setTypeEvent(typeEvent.getEvent());
        try {
            String topicName = "product.events";
            kafkaTemplate.send(topicName, event.getId(), event);
            log.info("Sent product event to Kafka: {}", event.getId());
        } catch (Exception e) {
            log.error("Failed to send product event to Kafka for ID {}: {}", event.getId(), e.getMessage(), e);
        }
    }
}
