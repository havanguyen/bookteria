package com.hanguyen.order_service.service;

import com.hanguyen.order_service.dto.ApiResponse;
import com.hanguyen.order_service.dto.event.InitiatePaymentCommand;
import com.hanguyen.order_service.dto.event.OrderItemDto;
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
import com.hanguyen.order_service.utils.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE , makeFinal = true)
public class OrderService {

    CartClient cartClient;
    OrderRepository orderRepository;
    OrderItemRepository orderItemRepository;
    SagaProducerService sagaProducerService;

    public Orders createOrder(CheckoutRequest checkoutRequest){
        String userId = SecurityUtils.getUserId();

        ApiResponse<List<CartResponse>> listApiResponse = cartClient.getCart();

        List<CartResponse> cartResponses = (listApiResponse != null ) ? listApiResponse.getResult() : null ;

        List<OrderItem> orderItems = new ArrayList<>();
        List<OrderItemDto> orderItemDtos = new ArrayList<>();
        Double amountTotal = 0.0;
        if (cartResponses != null && !cartResponses.isEmpty()){
            for (CartResponse cartResponse : cartResponses){
                Double priceAtPurchase = (cartResponse.getProductResponse().getSalePrice() != null )
                        ? cartResponse.getProductResponse().getSalePrice()
                        : cartResponse.getProductResponse().getBasePrice();

                OrderItem orderItem = OrderItem.builder()
                        .productId(cartResponse.getProductResponse().getId())
                        .quantity(cartResponse.getQuantity())
                        .priceAtPurchase(priceAtPurchase)
                        .build();

                OrderItemDto orderItemDto = OrderItemDto.builder()
                        .bookId(cartResponse.getProductResponse().getId())
                        .quantity(cartResponse.getQuantity())
                        .build();

                orderItems.add(orderItem);
                orderItemDtos.add(orderItemDto);
                amountTotal += priceAtPurchase;
            }

            Orders orders = Orders.builder()
                    .items(orderItems)
                    .shippingAddress(checkoutRequest.getShippingAddress())
                    .userId(userId)
                    .orderStatus(OrderStatus.PENDING)
                    .totalAmount(amountTotal)
                    .note(checkoutRequest.getNote())
                    .build();

            orderRepository.save(orders);

            for (OrderItem orderItem : orderItems){
                orderItem.setOrders(orders);
                orderItemRepository.save(orderItem);
            }
            log.info("Order {} saved with PENDING status", orders.getId());

            ApiResponse<String> stringApiResponse = cartClient.deleteCart();
            log.info("Cart deleted for user: {} , message {}", userId , stringApiResponse);

            ReserveInventoryCommand reserveInventoryCommand = ReserveInventoryCommand.builder()
                    .orderId(orders.getId())
                    .items(orderItemDtos)
                    .build();

            sagaProducerService.sendReserveInventoryCommand(reserveInventoryCommand);
            log.info("Send command reverse inventory for order id {}" , orders.getId());


            HttpServletRequest request = Optional.ofNullable(RequestContextHolder.getRequestAttributes())
                    .filter(ServletRequestAttributes.class::isInstance)
                    .map(ServletRequestAttributes.class::cast)
                    .map(ServletRequestAttributes::getRequest)
                    .orElse(null);

            String ipAddress = null;
            if (request != null) {
                ipAddress = request.getHeader("X-Forwarded-For");

                if (ipAddress != null && !ipAddress.isEmpty() && !"unknown".equalsIgnoreCase(ipAddress)) {
                    ipAddress = ipAddress.split(",")[0].trim();
                } else {
                    ipAddress = request.getRemoteAddr();
                }
            }

            if (ipAddress == null ||
                    ipAddress.equals("127.0.0.1") ||
                    ipAddress.equals("0:0:0:0:0:0:0:1") ||
                    ipAddress.startsWith("0:0:0:0:0:0:0:1%")) {
                ipAddress = SecurityUtils.getIpAddress();
            }
            InitiatePaymentCommand initiatePaymentCommand = InitiatePaymentCommand.builder()
                    .orderId(orders.getId())
                    .totalAmount(orders.getTotalAmount())
                    .ipAddress(ipAddress)
                    .build();

            sagaProducerService.sendInitiatePaymentCommand(initiatePaymentCommand);
            return orders;
        }
        else {
            throw new AppException(ErrorCode.CART_IS_EMPTY);
        }
    }
}