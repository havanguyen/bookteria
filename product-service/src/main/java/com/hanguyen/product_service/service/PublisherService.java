package com.hanguyen.product_service.service;

import com.hanguyen.product_service.dto.event.TypeEvent;
import com.hanguyen.product_service.dto.request.PublisherRequest;
import com.hanguyen.product_service.dto.response.PublisherResponse;
import com.hanguyen.product_service.entity.Product;
import com.hanguyen.product_service.entity.Publisher;
import com.hanguyen.product_service.exception.AppException;
import com.hanguyen.product_service.exception.ErrorCode;
import com.hanguyen.product_service.mapper.PublisherMapper;
import com.hanguyen.product_service.repository.ProductRepository;
import com.hanguyen.product_service.repository.PublisherRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE , makeFinal = true)
public class PublisherService {

    PublisherRepository publisherRepository;
    PublisherMapper publisherMapper;
    ProductRepository productRepository;
    ProductEventProducerService productEventProducerService;

    @Transactional
    public PublisherResponse create(PublisherRequest request) {
        Publisher publisher = publisherMapper.toPublisher(request);
        publisher = publisherRepository.save(publisher);
        return publisherMapper.toPublisherResponse(publisher);
    }

    @Transactional
    public PublisherResponse update(String id, PublisherRequest request) {
        Publisher publisher = publisherRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.ID_PUBLISH_NOT_FOUND));

        List<Product> affectedProduct =  productRepository.findAllByPublisherId(id);
        publisher.setName(request.getName());
        publisher = publisherRepository.save(publisher);

        for(Product product : affectedProduct){
            product.setPublisher(publisher);
            productEventProducerService.sendProductEvent(product , TypeEvent.UPDATE);
        }
        return publisherMapper.toPublisherResponse(publisher);
    }

    public Page<PublisherResponse> getAll(Pageable pageable) {
        return publisherRepository.findAll(pageable)
                .map(publisherMapper::toPublisherResponse);
    }

    public PublisherResponse getById(String id) {
        return publisherRepository.findById(id)
                .map(publisherMapper::toPublisherResponse)
                .orElseThrow(() -> new AppException(ErrorCode.ID_PUBLISH_NOT_FOUND));
    }

    @Transactional
    public void delete(String id) {
        if (!publisherRepository.existsById(id)) {
            throw new AppException(ErrorCode.ID_PUBLISH_NOT_FOUND);
        }
        List<Product> affectedProduct =  productRepository.findAllByPublisherId(id);
        for(Product product : affectedProduct){
            product.setPublisher(null);
            productEventProducerService.sendProductEvent(product , TypeEvent.UPDATE);
        }
        publisherRepository.deleteById(id);
    }
}