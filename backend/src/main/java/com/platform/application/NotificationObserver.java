package com.platform.application;

/**
 * Observer Pattern — Concrete Observer.
 * Listens to {@link BookingEvent}s and delegates to {@link NotificationService}
 * to persist and print notifications.
 */
public class NotificationObserver implements BookingEventObserver {

    private final NotificationService notificationService;

    public NotificationObserver(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Override
    public void onBookingEvent(BookingEvent event) {
        String recipientId = event.getBooking().getClientId();
        notificationService.send(recipientId, event.getMessage());
    }
}
