package com.hanguyen.inventory_service.dto.request;


import lombok.*;
import lombok.experimental.FieldDefaults;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InventoryRequest {
    String bookId;
    int quantity;
}
