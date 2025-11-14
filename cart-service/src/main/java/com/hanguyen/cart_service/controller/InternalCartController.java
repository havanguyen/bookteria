package com.hanguyen.cart_service.controller;


import com.hanguyen.cart_service.dto.ApiResponse;
import com.hanguyen.cart_service.service.CartService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal")
@FieldDefaults(level = AccessLevel.PRIVATE , makeFinal = true)
public class InternalCartController {

    CartService cartService;

    @DeleteMapping("/carts/user/{userId}")
    public ApiResponse<String> deleteCartByUserId(@PathVariable String userId) {
        cartService.deleteCartByUserId(userId);
        return ApiResponse.<String>builder()
                .result("Cart deleted successfully for user " + userId)
                .build();
    }
}
