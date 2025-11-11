package com.hanguyen.product_service.dto.response;


import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.OffsetDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PublisherResponse {
    String id;
    String name;
    OffsetDateTime createdAt;
    OffsetDateTime updatedAt;
}
