package com.hanguyen.order_service.controller;

import com.hanguyen.order_service.dto.ApiResponse;
import com.hanguyen.order_service.dto.request.CheckoutRequest;
import com.hanguyen.order_service.entity.Orders;
import com.hanguyen.order_service.exception.AppException;
import com.hanguyen.order_service.exception.ErrorCode;
import com.hanguyen.order_service.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/checkout")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Orders> checkout(@RequestBody CheckoutRequest checkoutRequest , HttpServletRequest request) {
        checkoutRequest.setIpAddress(request.getRemoteAddr());
        Orders orders = orderService.createOrder(checkoutRequest);
        return ApiResponse.<Orders>builder()
                .message("Order created successfully, processing.")
                .result(orders)
                .build();
    }

    @GetMapping("/{orderId}/payment-url")
    public ApiResponse<String> getPaymentUrl(@PathVariable String orderId) {

        String paymentUrl = orderService.getPaymentUrl(orderId);
        return ApiResponse.<String>builder()
                .result(paymentUrl).build();
    }
}