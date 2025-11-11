package com.hanguyen.cart_service.repository.client;


import com.hanguyen.cart_service.dto.ApiResponse;
import com.hanguyen.cart_service.dto.response.ProductResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "product-service")
public interface ProductClient {
    @GetMapping(value = "/products/{id}" , produces = MediaType.APPLICATION_JSON_VALUE)
    ApiResponse<ProductResponse> getProductByBookId(@PathVariable("id") String id);
}
