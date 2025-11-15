package com.hanguyen.order_service.controller;

import com.hanguyen.order_service.dto.ApiResponse;
import com.hanguyen.order_service.dto.request.CheckoutRequest;
import com.hanguyen.order_service.entity.Orders;
import com.hanguyen.order_service.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping()
    public ApiResponse<List<Orders>> getAllOrders() {
        List<Orders> orders = orderService.getAllOrders();
        return ApiResponse.<List<Orders>>builder()
                .result(orders)
                .build();
    }

    @GetMapping("/my-orders")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<Orders>> getMyOrders() {
        List<Orders> orders = orderService.getMyOrders();
        return ApiResponse.<List<Orders>>builder()
                .result(orders)
                .build();
    }

    @GetMapping("/{orderId}")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Orders> getMyOrders(@PathVariable String orderId) {
        Orders orders = orderService.getOrderById(orderId);
        return ApiResponse.<Orders>builder()
                .result(orders)
                .build();
    }

    @GetMapping("/{orderId}/payment-url")
    public ApiResponse<String> getPaymentUrl(@PathVariable String orderId) {

        String paymentUrl = orderService.getPaymentUrl(orderId);
        return ApiResponse.<String>builder()
                .result(paymentUrl).build();
    }

    @DeleteMapping("/{orderId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<String> deleteOrderById(@PathVariable String orderId) {
         orderService.deleteOrderById(orderId);
        return ApiResponse.<String>builder()
                .result(String.format("Delete order has id %s successfully" , orderId))
                .build();
    }
}