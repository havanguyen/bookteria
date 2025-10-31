package com.hanguyen.product_service.dto.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductEvent {
    private String id;
    private String title;
    private String description;
    private double basePrice;
    private String imageUrl;
    private String authorName;
    private String publisherName;
    private List<String> categoryNames;
}