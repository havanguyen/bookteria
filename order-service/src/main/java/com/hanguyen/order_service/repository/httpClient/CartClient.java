package com.hanguyen.order_service.repository.httpClient;


import com.hanguyen.order_service.configuration.AuthenticationRequestInterceptor;
import com.hanguyen.order_service.dto.ApiResponse;
import com.hanguyen.order_service.dto.response.CartResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "cart-service", configuration ={
        AuthenticationRequestInterceptor.class
})
public interface CartClient {

    @GetMapping(value = "/my-cart" , produces = MediaType.APPLICATION_JSON_VALUE)
    ApiResponse<List<CartResponse>> getCart();

    @DeleteMapping(value = "/my-cart")
    ApiResponse<String> deleteCart();

    @DeleteMapping(value = "/internal/carts/user/{userId}")
    ApiResponse<String> deleteCartByUserId(@PathVariable("userId") String userId);
}
