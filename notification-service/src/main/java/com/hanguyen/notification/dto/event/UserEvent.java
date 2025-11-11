package com.hanguyen.notification.dto.event;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserEvent {
    String userId;
    String username;
    String email;
    String firstName;
    String typeEvent;
}
