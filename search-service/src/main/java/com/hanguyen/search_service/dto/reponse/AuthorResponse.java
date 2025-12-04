package com.hanguyen.search_service.dto.reponse;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.OffsetDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AuthorResponse {
    String id;
    String name;
    private String bio;
    private String avatarUrl;

    OffsetDateTime createdAt;
    OffsetDateTime updatedAt;
}