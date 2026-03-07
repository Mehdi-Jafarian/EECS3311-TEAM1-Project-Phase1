package com.platform.infrastructure.repository;

import com.platform.domain.Notification;

import java.util.List;

public interface NotificationRepository {
    void save(Notification notification);
    List<Notification> findByRecipientId(String recipientId);
    List<Notification> findAll();
}
