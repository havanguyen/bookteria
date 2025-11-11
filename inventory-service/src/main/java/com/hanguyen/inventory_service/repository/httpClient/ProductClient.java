package com.hanguyen.inventory_service.repository.httpClient;


import com.hanguyen.inventory_service.dto.ApiResponse;
import com.hanguyen.inventory_service.dto.reponse.ProductResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "product-service")
public interface ProductClient {

    @GetMapping(value = "/products/{id}")
    ApiResponse<ProductResponse> getProductById(@PathVariable("id") String id);
}
