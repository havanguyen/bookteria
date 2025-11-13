package com.hanguyen.payment_service.dto.reply;


import lombok.*;
import lombok.experimental.FieldDefaults;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderReserverReply {
    String orderId;
    String message;
}
