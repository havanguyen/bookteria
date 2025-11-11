package com.hanguyen.payment_service.controller;

import com.hanguyen.payment_service.dto.ApiResponse;
import com.hanguyen.payment_service.dto.reponse.VNPayIpnResponse;
import com.hanguyen.payment_service.service.PaymentProcessingService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequiredArgsConstructor
@RequestMapping("/payment")
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
}
