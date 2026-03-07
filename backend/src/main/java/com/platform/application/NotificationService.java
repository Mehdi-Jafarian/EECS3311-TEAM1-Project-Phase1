package com.platform.application;

import com.platform.domain.Notification;
import com.platform.infrastructure.TimeProvider;
import com.platform.infrastructure.repository.NotificationRepository;

import java.util.List;
import java.util.UUID;

/**
 * Application service for UC notifications.
 * Stores each notification in-memory and prints it to the console.
 */
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final TimeProvider timeProvider;

    public NotificationService(NotificationRepository notificationRepository,
                                TimeProvider timeProvider) {
        this.notificationRepository = notificationRepository;
        this.timeProvider = timeProvider;
    }

    /**
     * Creates, persists, and prints a notification to the given recipient.
     *
     * @param recipientId ID of the recipient (client or consultant)
     * @param message     notification text
     * @return the created {@link Notification}
     */
    public Notification send(String recipientId, String message) {
        Notification notification = new Notification(
                UUID.randomUUID().toString(),
                recipientId,
                message,
                timeProvider.now()
        );
        notificationRepository.save(notification);
        System.out.println(notification);
        return notification;
    }

    /**
     * Returns all notifications for a given recipient.
     *
     * @param recipientId the recipient's ID
     * @return list of notifications (may be empty)
     */
    public List<Notification> getNotifications(String recipientId) {
        return notificationRepository.findByRecipientId(recipientId);
    }
}
