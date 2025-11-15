package com.hanguyen.payment_service.service;

import com.hanguyen.payment_service.configuration.RabbitMQProperties;
import com.hanguyen.payment_service.dto.event.PaymentFailedEvent;
import com.hanguyen.payment_service.dto.event.PaymentSucceededEvent;
import com.hanguyen.payment_service.dto.reponse.VNPayIpnResponse;
import com.hanguyen.payment_service.entity.PaymentStatus;
import com.hanguyen.payment_service.entity.PaymentTransactions;
import com.hanguyen.payment_service.exception.AppException;
import com.hanguyen.payment_service.exception.ErrorCode;
import com.hanguyen.payment_service.repository.PaymentRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE , makeFinal = true)
public class PaymentProcessingService {

    PaymentRepository transactionRepository;
    VNPayService vnPayService;
    RabbitTemplate rabbitTemplate;

    RabbitMQProperties rabbitMQProperties;

    @Transactional
    public VNPayIpnResponse processIpn(HttpServletRequest request) {
        Map<String, String> paramsMap = vnPayService.getParamsMap(request);
        log.info("Processing IPN: {}", paramsMap);

        String vnp_TxnRef = paramsMap.get("vnp_TxnRef");
        String vnp_ResponseCode = paramsMap.get("vnp_ResponseCode");
        String vnp_Amount = paramsMap.get("vnp_Amount");

        Map<String, String> rawParamsMap = new HashMap<>();
        request.getParameterMap().forEach((key, values) -> {
            if (values.length > 0) {
                rawParamsMap.put(key, values[0]);
            }
        });

        if (!vnPayService.validateSignature(rawParamsMap)) {
            log.warn("Invalid Checksum for TxnRef: {}", vnp_TxnRef);
            return new VNPayIpnResponse("97", "Invalid Checksum");
        }

        PaymentTransactions tx = transactionRepository.findByExternalTransactionId(vnp_TxnRef).orElse(null);
        if (tx == null) {
            log.warn("Order not found for TxnRef: {}", vnp_TxnRef);
            return new VNPayIpnResponse("01", "Order not Found");
        }

        Double vnpAmount = Double.parseDouble(vnp_Amount) / 100;
        double epsilon = 0.0001;
        if (Math.abs(tx.getAmount() - vnpAmount) > epsilon) {
            log.warn("Invalid amount for TxnRef: {}. Expected: {}, Got: {}", vnp_TxnRef, tx.getAmount(), vnpAmount);
            return new VNPayIpnResponse("04", "Invalid Amount");
        }

        if (tx.getPaymentStatus() != PaymentStatus.PENDING) {
            log.warn("Order already confirmed for TxnRef: {}", vnp_TxnRef);
            return new VNPayIpnResponse("02", "Order already confirmed");
        }

        try {
            Map<String, Object> gatewayResponse = new HashMap<>(paramsMap);
            tx.setGatewayResponse(gatewayResponse);

            if ("00".equals(vnp_ResponseCode)) {
                log.info("Payment SUCCESS for TxnRef: {}", vnp_TxnRef);
                tx.setPaymentStatus(PaymentStatus.SUCCESS);
                transactionRepository.save(tx);

                rabbitTemplate.convertAndSend(
                        rabbitMQProperties.getExchanges().getPayment(),
                        rabbitMQProperties.getRoutingKeys().getPaymentReply(),
                        PaymentSucceededEvent.builder()
                                .orderId(tx.getOrderId())
                                .message("SUCCESS")
                                .build()
                );
            } else {
                log.info("Payment FAILED for TxnRef: {}", vnp_TxnRef);
                tx.setPaymentStatus(PaymentStatus.FAILED);
                transactionRepository.save(tx);

                rabbitTemplate.convertAndSend(
                        rabbitMQProperties.getExchanges().getPayment(),
                        rabbitMQProperties.getRoutingKeys().getPaymentReply(),
                        PaymentFailedEvent.builder()
                                .orderId(tx.getOrderId())
                                .message("FAILED")
                                .build()
                );
            }
            return new VNPayIpnResponse("00", "Confirm Success");

        } catch (Exception e) {
            log.error("Unknown error processing TxnRef: {}", vnp_TxnRef, e);
            return new VNPayIpnResponse("99", "Unknown error");
        }
    }

    public URI processCallback(HttpServletRequest request) {
        Map<String, String> paramsMap = vnPayService.getParamsMap(request);
        String vnp_TxnRef = paramsMap.get("vnp_TxnRef");
        String vnp_ResponseCode = paramsMap.get("vnp_ResponseCode");
        String orderId = "unknown";

        Map<String, String> rawParamsMap = new HashMap<>();
        request.getParameterMap().forEach((key, values) -> {
            if (values.length > 0) {
                rawParamsMap.put(key, values[0]);
            }
        });

        String frontendUrl = "https://bookteria.xyz/payment/result";

        boolean isValidSignature = vnPayService.validateSignature(rawParamsMap);
        if (!isValidSignature) {
            log.warn("Invalid Checksum for Callback TxnRef: {}", vnp_TxnRef);
            return URI.create(frontendUrl + "?orderId=" + orderId + "&code=97"); // 97 = Invalid Signature
        }

        PaymentTransactions tx = transactionRepository.findByExternalTransactionId(vnp_TxnRef).orElse(null);
        if (tx != null) {
            orderId = tx.getOrderId();
        }

        return URI.create(frontendUrl + "?orderId=" + orderId + "&code=" + vnp_ResponseCode);
    }

    public List<PaymentTransactions> getAllPayment(){
        return transactionRepository.findAll();
    }

    public PaymentTransactions getPaymentById(String paymentId){
        return transactionRepository.findById(paymentId).orElseThrow(() -> new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION));
    }
}