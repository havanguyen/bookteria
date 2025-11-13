package com.hanguyen.order_service.dto.reply;


import lombok.*;
import lombok.experimental.FieldDefaults;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InventoryOutOfStockReply {
    String orderId;
    String bookId;
    String message;
    int quantity;
}
