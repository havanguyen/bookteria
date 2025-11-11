package com.hanguyen.inventory_service.dto.event;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductEvent {
    String id;
    String title;
    String description;
    double basePrice;
    String imageUrl;
    String authorName;
    String publisherName;
    List<String> categoryNames;
    String typeEvent;
}