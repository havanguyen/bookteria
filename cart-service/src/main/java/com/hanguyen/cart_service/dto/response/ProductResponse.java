package com.hanguyen.cart_service.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.OffsetDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductResponse {
    String id;
    String title;
    String description;
    String imageUrl;
    Integer pageCount;
    Double basePrice;
    Double salePrice;
    OffsetDateTime saleStartDate;
    OffsetDateTime saleEndDate;
}