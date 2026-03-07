package com.platform.infrastructure.repository;

import com.platform.domain.Notification;

import java.util.*;
import java.util.stream.Collectors;

public class InMemoryNotificationRepository implements NotificationRepository {

    private final List<Notification> store = new ArrayList<>();

    @Override
    public void save(Notification notification) {
        store.add(notification);
    }

    @Override
    public List<Notification> findByRecipientId(String recipientId) {
        return store.stream()
                .filter(n -> n.getRecipientId().equals(recipientId))
                .collect(Collectors.toList());
    }

    @Override
    public List<Notification> findAll() {
        return Collections.unmodifiableList(store);
    }
}
