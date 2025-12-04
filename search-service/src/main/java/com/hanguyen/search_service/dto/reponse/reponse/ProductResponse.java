package com.hanguyen.search_service.dto.reponse;

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
public class ProductResponse {
    String id;
    String title;
    String slug;
    String isbn;
    String description;
    String imageUrl;
    AuthorResponse author;
    PublisherResponse publisher;
    LocalDate publicationDate;
    Integer pageCount;
    Double basePrice;
    Double salePrice;
    OffsetDateTime saleStartDate;
    OffsetDateTime saleEndDate;
    Map<String, Object> attributes;
    Set<CategoryResponse> categories;
    OffsetDateTime createdAt;
    OffsetDateTime updatedAt;
}
