package com.hanguyen.identity.dto.request;

import java.time.LocalDate;

import com.hanguyen.identity.validator.DobConstraint;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProfileUpdateRequest {
    String firstName;
    String lastName;

    @DobConstraint(min = 10, message = "INVALID_DOB")
    LocalDate dob;

    String email;
    String city;
    String avatarUrl;
}
