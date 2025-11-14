package com.hanguyen.order_service.dto.reply;


import lombok.*;
import lombok.experimental.FieldDefaults;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaymentInitiatedReply {
    String orderId;
    String paymentUrl;
    String message;
}
