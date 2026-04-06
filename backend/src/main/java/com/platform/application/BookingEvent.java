package com.platform.application;

import com.platform.domain.Booking;

/**
 * Observer Pattern — event carrying a booking and a descriptive message.
 * Published by {@link BookingService} and {@link PaymentService}.
 */
public class BookingEvent {

    private final Booking booking;
    private final String eventType;
    private final String message;

    public BookingEvent(Booking booking, String eventType, String message) {
        this.booking = booking;
        this.eventType = eventType;
        this.message = message;
    }

    public Booking getBooking()   { return booking; }
    public String  getEventType() { return eventType; }
    public String  getMessage()   { return message; }
}
