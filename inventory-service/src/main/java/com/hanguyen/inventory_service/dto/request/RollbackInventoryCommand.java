package com.hanguyen.inventory_service.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RollbackInventoryCommand {
    String orderId;
    List<OrderItemDto> items;
}