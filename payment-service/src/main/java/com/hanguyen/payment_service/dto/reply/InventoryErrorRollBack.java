package com.hanguyen.payment_service.dto.reply;


import lombok.*;
import lombok.experimental.FieldDefaults;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InventoryErrorRollBack {
    String orderId;
    String bookId;
    String message;
}
