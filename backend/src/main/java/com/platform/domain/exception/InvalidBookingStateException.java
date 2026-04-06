package com.platform.domain.exception;

/** Thrown when an operation is not valid for the current booking state. */
public class InvalidBookingStateException extends RuntimeException {
    public InvalidBookingStateException(String message) {
        super(message);
    }
}
