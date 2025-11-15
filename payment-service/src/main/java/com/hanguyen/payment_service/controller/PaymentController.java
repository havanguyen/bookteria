package com.hanguyen.payment_service.controller;

import com.hanguyen.payment_service.dto.ApiResponse;
import com.hanguyen.payment_service.dto.reponse.VNPayIpnResponse;
import com.hanguyen.payment_service.entity.PaymentTransactions;
import com.hanguyen.payment_service.service.PaymentProcessingService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/payment")
@FieldDefaults(level = AccessLevel.PRIVATE , makeFinal = true)
public class PaymentController {

    PaymentProcessingService paymentProcessingService;

    @PostMapping("/ipn/vnpay")
    public ApiResponse<VNPayIpnResponse> handleVNPayIPN(HttpServletRequest request) {
        VNPayIpnResponse ipnResponse = paymentProcessingService.processIpn(request);
        return ApiResponse.<VNPayIpnResponse>builder()
                .result(ipnResponse)
                .build();
    }

    @GetMapping("/callback/vnpay")
    public ResponseEntity<Void> handleVNPayCallback(HttpServletRequest request) {
        URI redirectUri = paymentProcessingService.processCallback(request);
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(redirectUri)
                .build();
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<PaymentTransactions>> getAllPayment(){
        return ApiResponse.<List<PaymentTransactions>>builder()
                .result(paymentProcessingService.getAllPayment())
                .build();
    }

    @GetMapping("/{paymentId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<PaymentTransactions> getPaymentByID(@PathVariable String paymentId){
        return ApiResponse.<PaymentTransactions>builder()
                .result(paymentProcessingService.getPaymentById(paymentId))
                .build();
    }

}
