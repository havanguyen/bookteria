package com.hanguyen.search_service.repository.httpClient;

import com.hanguyen.search_service.dto.reponse.ApiResponse;
import com.hanguyen.search_service.dto.reponse.ProductResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "product-service")
public interface ProductClient {
    @GetMapping("/products")
    ApiResponse<Page<ProductResponse>> getAllProducts(Pageable pageable);
}
