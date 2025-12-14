package com.hanguyen.notification.entity;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.*;

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
