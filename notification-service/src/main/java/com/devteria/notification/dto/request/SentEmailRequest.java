package com.devteria.notification.dto.request;


import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SentEmailRequest {
    List<Recipient> recipients;
    String subject;
    String htmlContent;
}
