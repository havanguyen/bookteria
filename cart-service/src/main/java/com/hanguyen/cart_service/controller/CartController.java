package com.hanguyen.cart_service.controller;


import com.hanguyen.cart_service.dto.ApiResponse;
import com.hanguyen.cart_service.dto.request.CartRequest;
import com.hanguyen.cart_service.dto.response.CartResponse;
import com.hanguyen.cart_service.service.CartService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/my-cart")
@FieldDefaults(level = AccessLevel.PRIVATE , makeFinal = true)
public class CartController {

    CartService cartService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    ApiResponse<CartResponse> addToCart(@RequestBody CartRequest cartRequest){
        CartResponse cartResponse = cartService.addProductToCart(cartRequest);
        return ApiResponse.<CartResponse>builder()
                .result(cartResponse)
                .build();
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    ApiResponse<List<CartResponse>> getProductsInMyCart(){
        return ApiResponse.<List<CartResponse>>builder()
                .result(cartService.getCart())
                .build();
    }

    @PutMapping
    @PreAuthorize("isAuthenticated()")
    ApiResponse<CartResponse> updateCart(@RequestBody CartRequest cartRequest ){
        return ApiResponse.<CartResponse>builder()
                .result(cartService.updateQuantity(cartRequest))
                .build();
    }

    @DeleteMapping("/{bookId}")
    @PreAuthorize("isAuthenticated()")
    ApiResponse<String> removeProductFromCart(@PathVariable("bookId") String bookId){
        cartService.removeFromCart(bookId);
        return ApiResponse.<String>builder()
                .result(String.format("Remove product with id %s successfully" , bookId))
                .build();
    }

    @DeleteMapping
    @PreAuthorize("isAuthenticated()")
    ApiResponse<String> removeCart(){
        cartService.clearCart();
        return ApiResponse.<String>builder()
                .result("Remove card successfully" )
                .build();
    }
}


