package com.hanguyen.order_service.dto.reply;


import com.hanguyen.order_service.dto.event.OrderItemDto;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

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
    private List<OrderItemDto> itemsToRollback;
}
