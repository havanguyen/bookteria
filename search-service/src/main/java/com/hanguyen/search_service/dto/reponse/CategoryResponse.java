package com.hanguyen.search_service.dto.reponse;


import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.OffsetDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CategoryResponse {
    String id;
    String name;
    String slug;
    OffsetDateTime createdAt;
    OffsetDateTime updatedAt;
}
