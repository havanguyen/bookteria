package com.hanguyen.inventory_service.dto.event;


import lombok.*;
import lombok.experimental.FieldDefaults;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InventoryUpdateEvent {
    String bookId;
    int newStock;
}
