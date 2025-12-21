package com.hanguyen.payment_service.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import jakarta.servlet.http.HttpServletRequest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import com.hanguyen.payment_service.configuration.RabbitMQProperties;
import com.hanguyen.payment_service.dto.event.PaymentSucceededEvent;
import com.hanguyen.payment_service.dto.reponse.VNPayIpnResponse;
import com.hanguyen.payment_service.entity.PaymentStatus;
import com.hanguyen.payment_service.entity.PaymentTransactions;
import com.hanguyen.payment_service.repository.PaymentRepository;
import com.hanguyen.payment_service.utils.TestUtils;

@ExtendWith(MockitoExtension.class)
public class PaymentProcessingServiceTest {

    @Mock
    private PaymentRepository transactionRepository;

    @Mock
    private VNPayService vnPayService;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Mock
    private RabbitMQProperties rabbitMQProperties;

    @Mock
    private RabbitMQProperties.Exchanges exchanges;

    @Mock
    private RabbitMQProperties.RoutingKeys routingKeys;

    @InjectMocks
    private PaymentProcessingService paymentProcessingService;

    private PaymentTransactions paymentTransaction;

    @BeforeEach
    void initData() {
        paymentTransaction = TestUtils.getObject("data/payment/payment_transaction.json", PaymentTransactions.class);
    }

    private void mockRabbitMQProperties() {
        when(rabbitMQProperties.getExchanges()).thenReturn(exchanges);
        when(exchanges.getOrder()).thenReturn("order_exchange");
        when(rabbitMQProperties.getRoutingKeys()).thenReturn(routingKeys);
    }

    @Test
    void processIpn_success() {
        mockRabbitMQProperties();
        when(routingKeys.getPaymentSuccessReply()).thenReturn("success_key");

        HttpServletRequest request = mock(HttpServletRequest.class);
        Map<String, String> params = new HashMap<>();
        params.put("vnp_TxnRef", "123456");
        params.put("vnp_ResponseCode", "00");
        params.put("vnp_Amount", "10000"); // 100 * 100

        when(vnPayService.getParamsMap(request)).thenReturn(params);

        Map<String, String[]> rawParams = new HashMap<>();
        rawParams.put("key", new String[] { "value" });
        when(request.getParameterMap()).thenReturn(rawParams);

        when(vnPayService.validateSignature(any())).thenReturn(true);
        when(transactionRepository.findByExternalTransactionId("123456")).thenReturn(Optional.of(paymentTransaction));
        when(transactionRepository.save(any(PaymentTransactions.class))).thenReturn(paymentTransaction);

        VNPayIpnResponse response = paymentProcessingService.processIpn(request);

        assertEquals("00", response.getRspCode());
        assertEquals("Confirm Success", response.getMessage());
        assertEquals(PaymentStatus.SUCCESS, paymentTransaction.getPaymentStatus());

        verify(rabbitTemplate, times(1)).convertAndSend(eq("order_exchange"), eq("success_key"),
                any(PaymentSucceededEvent.class));
    }

    @Test
    void processIpn_invalidChecksum_fail() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        Map<String, String> params = new HashMap<>();
        params.put("vnp_TxnRef", "123456");

        when(vnPayService.getParamsMap(request)).thenReturn(params);
        when(request.getParameterMap()).thenReturn(new HashMap<>());
        when(vnPayService.validateSignature(any())).thenReturn(false);

        VNPayIpnResponse response = paymentProcessingService.processIpn(request);

        assertEquals("97", response.getRspCode());
    }

    @Test
    void processIpn_invalidAmount_fail() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        Map<String, String> params = new HashMap<>();
        params.put("vnp_TxnRef", "123456");
        params.put("vnp_Amount", "20000"); // 200 * 100 != 100

        when(vnPayService.getParamsMap(request)).thenReturn(params);
        when(request.getParameterMap()).thenReturn(new HashMap<>());
        when(vnPayService.validateSignature(any())).thenReturn(true);
        when(transactionRepository.findByExternalTransactionId("123456")).thenReturn(Optional.of(paymentTransaction));

        VNPayIpnResponse response = paymentProcessingService.processIpn(request);

        assertEquals("04", response.getRspCode());
    }

    @Test
    void processCallback_validSignature_success() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        Map<String, String> params = new HashMap<>();
        params.put("vnp_TxnRef", "123456");
        params.put("vnp_ResponseCode", "00");

        when(vnPayService.getParamsMap(request)).thenReturn(params);
        when(request.getParameterMap()).thenReturn(new HashMap<>());
        when(vnPayService.validateSignature(any())).thenReturn(true);
        when(transactionRepository.findByExternalTransactionId("123456")).thenReturn(Optional.of(paymentTransaction));

        URI uri = paymentProcessingService.processCallback(request);

        assertTrue(uri.toString().contains("orderId=order1"));
        assertTrue(uri.toString().contains("code=00"));
    }
}
