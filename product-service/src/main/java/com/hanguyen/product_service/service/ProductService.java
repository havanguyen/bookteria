package com.hanguyen.product_service.service;

import com.hanguyen.product_service.dto.event.ProductEvent;
import com.hanguyen.product_service.dto.request.ProductRequest;
import com.hanguyen.product_service.dto.response.ProductResponse;
import com.hanguyen.product_service.entity.Author;
import com.hanguyen.product_service.entity.Category;
import com.hanguyen.product_service.entity.Product;
import com.hanguyen.product_service.entity.Publisher;
import com.hanguyen.product_service.exception.AppException;
import com.hanguyen.product_service.exception.ErrorCode;
import com.hanguyen.product_service.mapper.ProductMapper;
import com.hanguyen.product_service.repository.AuthorRepository;
import com.hanguyen.product_service.repository.CategoryRepository;
import com.hanguyen.product_service.repository.ProductRepository;
import com.hanguyen.product_service.repository.PublisherRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE , makeFinal = true)
public class ProductService {

    AuthorRepository authorRepository;
    CategoryRepository categoryRepository;
    ProductRepository productRepository;
    PublisherRepository publisherRepository;
    ProductMapper productMapper;

    @NonFinal
    @Value("${app.kafka.topic}")
    String topicName;

    @NonFinal
    KafkaTemplate<String , ProductEvent> kafkaTemplate;

    @Transactional
    public ProductResponse createProduct(ProductRequest productRequest){
        Product product = productMapper.toProduct(productRequest);

        Author author = authorRepository.findById(productRequest.getAuthorId())
                .orElseThrow(() -> new AppException(ErrorCode.ID_AUTHOR_NOT_FOUND));

        Publisher publisher = publisherRepository.findById(productRequest.getPublisherId())
                .orElseThrow(() -> new AppException(ErrorCode.ID_PUBLISH_NOT_FOUND));

        product.setAuthor(author);
        product.setPublisher(publisher);

        if (productRequest.getCategoryIds() != null && !productRequest.getCategoryIds().isEmpty()) {
            Set<Category> categories = new HashSet<>(categoryRepository.findAllById(productRequest.getCategoryIds()));
            product.setCategories(categories);
        }

        Product savedProduct = productRepository.save(product);

        sendProductEvent(savedProduct);

        return productMapper.toProductResponse(savedProduct);
    }

    @Transactional
    public ProductResponse update(String id, ProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ID_PRODUCT_NOT_FOUND));

        product.setTitle(request.getTitle());
        product.setSlug(request.getSlug());
        product.setIsbn(request.getIsbn());
        product.setDescription(request.getDescription());
        product.setImageUrl(request.getImageUrl());
        product.setPublicationDate(request.getPublicationDate());
        product.setPageCount(request.getPageCount());
        product.setBasePrice(request.getBasePrice());
        product.setSalePrice(request.getSalePrice());
        product.setSaleStartDate(request.getSaleStartDate());
        product.setSaleEndDate(request.getSaleEndDate());
        product.setAttributes(request.getAttributes());

        if (!product.getAuthor().getId().equals(request.getAuthorId())) {
            Author author = authorRepository.findById(request.getAuthorId())
                    .orElseThrow(() -> new AppException(ErrorCode.ID_AUTHOR_NOT_FOUND));
            product.setAuthor(author);
        }

        if (!product.getPublisher().getId().equals(request.getPublisherId())) {
            Publisher publisher = publisherRepository.findById(request.getPublisherId())
                    .orElseThrow(() -> new AppException(ErrorCode.ID_AUTHOR_NOT_FOUND));
            product.setPublisher(publisher);
        }

        if (request.getCategoryIds() != null) {
            Set<Category> categories = new HashSet<>(categoryRepository.findAllById(request.getCategoryIds()));
            product.setCategories(categories);
        }

        Product savedProduct = productRepository.save(product);

        sendProductEvent(savedProduct);

        return productMapper.toProductResponse(savedProduct);
    }

    public Page<ProductResponse> getAll(Pageable pageable) {
        return productRepository.findAll(pageable)
                .map(productMapper::toProductResponse);
    }

    public ProductResponse getById(String id) {
        return productRepository.findById(id)
                .map(productMapper::toProductResponse)
                .orElseThrow(() -> new AppException(ErrorCode.ID_PRODUCT_NOT_FOUND));
    }

    public void delete(String id) {
        productRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ID_PRODUCT_NOT_FOUND));
        productRepository.deleteById(id);
    }

    private void sendProductEvent(Product product) {
        ProductEvent event = productMapper.toProductEvent(product);
        try {
            kafkaTemplate.send(topicName, event.getId(), event);
            log.info("Sent product event to Kafka: {}", event.getId());
        } catch (Exception e) {
            log.error("Failed to send product event to Kafka for ID {}: {}", event.getId(), e.getMessage(), e);
        }
    }
}
