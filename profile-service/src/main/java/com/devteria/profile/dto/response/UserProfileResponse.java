package com.devteria.profile.dto.response;

import java.time.LocalDate;
import java.util.UUID;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserProfileResponse {
    UUID id;
    String firstName;
    String lastName;
    LocalDate dob;
    String city;
}
