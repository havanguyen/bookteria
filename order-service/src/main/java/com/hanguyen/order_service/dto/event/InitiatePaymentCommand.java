package com.hanguyen.order_service.dto.event;


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
