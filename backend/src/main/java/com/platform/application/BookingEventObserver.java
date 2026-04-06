package com.platform.application;

/**
 * Observer Pattern — Observer interface.
 * Consumers (e.g., {@link NotificationObserver}) react to booking lifecycle events.
 */
public interface BookingEventObserver {
    void onBookingEvent(BookingEvent event);
}
