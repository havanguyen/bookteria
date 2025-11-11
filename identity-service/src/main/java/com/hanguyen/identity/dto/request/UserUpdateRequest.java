package com.hanguyen.identity.dto.request;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.constraints.Email;

import com.hanguyen.identity.validator.DobConstraint;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserUpdateRequest {
    String password;
    String firstName;
    String lastName;

    @Email(message = "INVALID_EMAIL_FORMAT")
    String email;

    @DobConstraint(min = 10, message = "INVALID_DOB")
    LocalDate dob;

    List<String> roles;
    String city;

    Boolean isActive;

    String avatarUrl;
}
