package com.hanguyen.order_service.controller;

import com.hanguyen.order_service.dto.ApiResponse;
import com.hanguyen.order_service.dto.request.CheckoutRequest;
import com.hanguyen.order_service.entity.Orders;
import com.hanguyen.order_service.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/checkout")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Orders> checkout(@RequestBody CheckoutRequest checkoutRequest) {
        Orders orders = orderService.createOrder(checkoutRequest);
        return ApiResponse.<Orders>builder()
                .message("Order created successfully, processing.")
                .result(orders)
                .build();
    }
}