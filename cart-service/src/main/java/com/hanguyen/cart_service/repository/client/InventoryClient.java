package com.hanguyen.cart_service.repository.client;


import com.hanguyen.cart_service.dto.ApiResponse;
import com.hanguyen.cart_service.dto.response.InventoryResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "inventory-service")
public interface InventoryClient {

    @GetMapping(value = "/inventory/{id}" , produces = MediaType.APPLICATION_JSON_VALUE)
    ApiResponse<InventoryResponse> getStockById(@PathVariable("id") String id);
}
