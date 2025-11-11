package com.hanguyen.order_service.dto.request;

import com.hanguyen.order_service.dto.ShippingAddress;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutRequest {
    private ShippingAddress shippingAddress;
    private String Note;
}