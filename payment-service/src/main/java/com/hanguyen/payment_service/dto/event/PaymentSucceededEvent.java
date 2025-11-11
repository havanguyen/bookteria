package com.hanguyen.payment_service.dto.event;


import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaymentSucceededEvent {
    String orderId;
    String message;
}
