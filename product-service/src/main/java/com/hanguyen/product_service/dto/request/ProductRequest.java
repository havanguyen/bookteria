package com.hanguyen.product_service.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductRequest {
    @NotEmpty
    String title;
    String slug;
    String isbn;
    String description;
    String imageUrl;

    @NotNull
    String authorId;

    @NotNull
    String publisherId;

    LocalDate publicationDate;
    Integer pageCount;

    @Min(0)
    Double basePrice;

    Double salePrice;
    OffsetDateTime saleStartDate;
    OffsetDateTime saleEndDate;

    Map<String, Object> attributes;
    Set<String> categoryIds;
}