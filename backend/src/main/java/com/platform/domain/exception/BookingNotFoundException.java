package com.platform.domain.exception;

/** Thrown when a booking with the given ID cannot be found. */
public class BookingNotFoundException extends RuntimeException {
    public BookingNotFoundException(String bookingId) {
        super("Booking not found: " + bookingId);
    }
}
