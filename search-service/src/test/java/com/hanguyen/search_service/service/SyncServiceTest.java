package com.hanguyen.search_service.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.hanguyen.search_service.document.ProductDocument;
import com.hanguyen.search_service.dto.reponse.ApiResponse;
import com.hanguyen.search_service.dto.reponse.ProductResponse;
import com.hanguyen.search_service.mapper.ProductMapper;
import com.hanguyen.search_service.repository.ProductDocumentRepository;
import com.hanguyen.search_service.repository.httpClient.ProductClient;
import com.hanguyen.search_service.utils.TestUtils;

@ExtendWith(MockitoExtension.class)
public class SyncServiceTest {

    @Mock
    private ProductClient productClient;

    @Mock
    private ProductDocumentRepository productDocumentRepository;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private SyncService syncService;

    private ProductResponse productResponse;
    private ProductDocument productDocument;

    @BeforeEach
    void initData() {
        productResponse = TestUtils.getObject("data/search/product_response.json", ProductResponse.class);
        productDocument = new ProductDocument();
        productDocument.setId("prod1");
    }

    @Test
    void syncAllProducts_success() {
        Page<ProductResponse> page = new PageImpl<>(Collections.singletonList(productResponse));
        ApiResponse<Page<ProductResponse>> apiResponse = new ApiResponse<>();
        apiResponse.setResult(page);

        // Mock first call returns data, second call (loop) probably shouldn't happen if
        // isLast is true
        // PageImpl defaults isLast to true if total <= size? No, relies on Pageable.
        // But PageImpl constructor without pageable: lists content.
        // Let's ensure it handles isLast correctly.

        when(productClient.getAllProducts(any(Pageable.class))).thenReturn(apiResponse);
        when(productMapper.toProductDocument(any(ProductResponse.class))).thenReturn(productDocument);

        syncService.syncAllProducts();

        verify(productDocumentRepository, atLeastOnce()).deleteAll();
        verify(productDocumentRepository, times(1)).saveAll(anyList());
        verify(productClient, times(1)).getAllProducts(any(Pageable.class));
    }
}
