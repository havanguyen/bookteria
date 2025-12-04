package com.hanguyen.search_service.service;


import com.hanguyen.search_service.document.ProductDocument;
import com.hanguyen.search_service.dto.reponse.ApiResponse;
import com.hanguyen.search_service.dto.reponse.ProductResponse;
import com.hanguyen.search_service.mapper.ProductMapper;
import com.hanguyen.search_service.repository.ProductDocumentRepository;
import com.hanguyen.search_service.repository.httpClient.ProductClient;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE , makeFinal = true)
public class SyncService {

    ProductClient productClient;
    ProductDocumentRepository productDocumentRepository;
    ProductMapper productMapper;

    public void syncAllProducts() {
        int page = 0;
        int size = 100;
        boolean hasNext = true;

        log.info("Start syncing data from Product Service to Elasticsearch...");

        productDocumentRepository.deleteAll();

        while (hasNext) {
            try {
                Pageable pageable = PageRequest.of(page , size);
                ApiResponse<Page<ProductResponse>> apiResponse = productClient.getAllProducts(pageable);
                Page<ProductResponse> pageData = apiResponse.getResult();

                List<ProductResponse> products = pageData.getContent();

                if (products.isEmpty()) {
                    break;
                }

                List<ProductDocument> documents = products.stream()
                        .map(productMapper::toProductDocument).toList();

                productDocumentRepository.saveAll(documents);

                log.info("Synced batch {} with {} products", page, documents.size());

                if (pageData.isLast()) {
                    hasNext = false;
                } else {
                    page++;
                }

            } catch (Exception e) {
                log.error("Error syncing batch page {}", page, e);
                break;
            }
        }
        log.info("Sync process completed!");
    }
}
