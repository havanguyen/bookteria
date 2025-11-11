package com.hanguyen.search_service.service;


import com.hanguyen.search_service.document.ProductDocument;
import com.hanguyen.search_service.dto.event.ProductEvent;
import com.hanguyen.search_service.dto.event.TypeEvent;
import com.hanguyen.search_service.mapper.ProductMapper;
import com.hanguyen.search_service.repository.ProductDocumentRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE , makeFinal = true)
public class ProductEventConsumer {

    ProductDocumentRepository productDocumentRepository;
    ProductMapper productMapper;

    @KafkaListener(topics = "product.events" , groupId = "search-group")
    public void handleProductEvent(@Payload ProductEvent event){
        log.info("Received product event: {}", event.getId());
        try {
            if(event.getTypeEvent().equals(TypeEvent.DELETE.getEvent())){
                productDocumentRepository.deleteById(event.getId());
                log.info("Deleted product document: {}", event.getId());
                return;
            }
            ProductDocument doc = productMapper.toProductDocument(event);
            productDocumentRepository.save(doc);
            log.info("Create / update product document: {}", doc.getId());
        } catch (Exception e) {
            log.error("Error indexing product document: {}", event.getId(), e);
        }
    }
}
