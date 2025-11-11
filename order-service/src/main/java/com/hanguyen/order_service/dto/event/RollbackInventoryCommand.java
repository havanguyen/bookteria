package com.hanguyen.order_service.dto.event;


import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RollbackInventoryCommand {
    String orderId;
    List<OrderItemDto> items;
}
