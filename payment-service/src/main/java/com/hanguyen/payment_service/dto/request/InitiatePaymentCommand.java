package com.hanguyen.payment_service.dto.request;


import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InitiatePaymentCommand {
    String orderId;
    Double totalAmount;
    private String ipAddress;
}
