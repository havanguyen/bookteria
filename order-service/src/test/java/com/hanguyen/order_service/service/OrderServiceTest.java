package com.hanguyen.order_service.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import com.hanguyen.order_service.dto.ApiResponse;
import com.hanguyen.order_service.dto.event.ReserveInventoryCommand;
import com.hanguyen.order_service.dto.request.CheckoutRequest;
import com.hanguyen.order_service.dto.response.CartResponse;
import com.hanguyen.order_service.entity.OrderItem;
import com.hanguyen.order_service.entity.OrderStatus;
import com.hanguyen.order_service.entity.Orders;
import com.hanguyen.order_service.exception.AppException;
import com.hanguyen.order_service.exception.ErrorCode;
import com.hanguyen.order_service.repository.OrderItemRepository;
import com.hanguyen.order_service.repository.OrderRepository;
import com.hanguyen.order_service.repository.httpClient.CartClient;
import com.hanguyen.order_service.utils.TestUtils;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

    @Mock
    private CartClient cartClient;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private SagaProducerService sagaProducerService;

    @InjectMocks
    private OrderService orderService;

    private CheckoutRequest checkoutRequest;
    private CartResponse[] cartResponses;
    private Orders order;

    @BeforeEach
    void initData() {
        checkoutRequest = TestUtils.getObject("data/order/checkout_request.json", CheckoutRequest.class);
        cartResponses = TestUtils.getObject("data/order/cart_responses.json", CartResponse[].class);

        order = Orders.builder()
                .id("order1")
                .userId("user1")
                .orderStatus(OrderStatus.PENDING)
                .totalAmount(50.0)
                .build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void mockSecurityContext() {
        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication authentication = mock(Authentication.class);

        java.time.Instant now = java.time.Instant.now();
        Jwt jwt = new Jwt("token", now, now.plusSeconds(30),
                java.util.Collections.singletonMap("alg", "none"),
                java.util.Collections.singletonMap("sub", "user1"));

        when(authentication.getPrincipal()).thenReturn(jwt);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void createOrder_validRequest_success() {
        mockSecurityContext();

        ApiResponse<List<CartResponse>> apiResponse = new ApiResponse<>();
        apiResponse.setResult(Arrays.asList(cartResponses));

        when(cartClient.getCart()).thenReturn(apiResponse);
        when(orderRepository.save(any(Orders.class))).thenReturn(order);
        when(orderItemRepository.save(any(OrderItem.class))).thenReturn(new OrderItem());

        Orders result = orderService.createOrder(checkoutRequest);

        assertNotNull(result);
        verify(orderRepository, times(1)).save(any(Orders.class));
        verify(orderItemRepository, times(1)).save(any(OrderItem.class)); // 1 item in json
        verify(sagaProducerService, times(1)).sendReserveInventoryCommand(any(ReserveInventoryCommand.class));
    }

    @Test
    void createOrder_emptyCart_throwsException() {
        mockSecurityContext();

        ApiResponse<List<CartResponse>> apiResponse = new ApiResponse<>();
        apiResponse.setResult(Collections.emptyList());

        when(cartClient.getCart()).thenReturn(apiResponse);

        assertThrows(AppException.class, () -> orderService.createOrder(checkoutRequest));
    }

    @Test
    void getMyOrders_success() {
        mockSecurityContext();
        when(orderRepository.findAllByUserId("user1")).thenReturn(Collections.singletonList(order));

        List<Orders> orders = orderService.getMyOrders();

        assertFalse(orders.isEmpty());
        assertEquals("order1", orders.get(0).getId());
    }

    @Test
    void updateStatusOrder_validId_success() {
        when(orderRepository.findById(anyString())).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Orders.class))).thenReturn(order);

        orderService.updateStatusOrder("order1", OrderStatus.COMPLETED);

        assertEquals(OrderStatus.COMPLETED, order.getOrderStatus());
        verify(orderRepository, times(1)).save(order);
    }

    @Test
    void deleteOrderById_validId_success() {
        doNothing().when(orderRepository).deleteById(anyString());

        orderService.deleteOrderById("order1");

        verify(orderRepository, times(1)).deleteById("order1");
    }
}
