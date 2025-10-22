package com.devteria.profile.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE )
public class UserProfileResponse {
    UUID id;
    String firstName ;
    String lastName ;
    LocalDate dob ;
    String city ;
}
