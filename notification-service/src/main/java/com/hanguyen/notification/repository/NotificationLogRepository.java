package com.hanguyen.notification.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.hanguyen.notification.entity.NotificationLog;

@Repository
public interface NotificationLogRepository extends MongoRepository<NotificationLog, String> {}
