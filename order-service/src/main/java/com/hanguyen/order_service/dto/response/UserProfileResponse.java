package com.hanguyen.order_service.dto.response;


import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserProfileResponse {
    UUID id;
    String userId;
    String firstName;
    String lastName;
    LocalDate dob;
    String city;
    String email;
    String avatarUrl;
}

