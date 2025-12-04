package com.hanguyen.notification.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "notification_logs")
public class NotificationLog {
    @Id
    private String id;

    private String recipientEmail;
    private String subject;
    private String bodyPreview;
    private LocalDateTime createdAt;
}
