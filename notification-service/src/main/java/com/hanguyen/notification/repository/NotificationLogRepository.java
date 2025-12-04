package com.hanguyen.notification.repository;

import com.hanguyen.notification.entity.NotificationLog;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationLogRepository extends MongoRepository<NotificationLog, String> {
}
