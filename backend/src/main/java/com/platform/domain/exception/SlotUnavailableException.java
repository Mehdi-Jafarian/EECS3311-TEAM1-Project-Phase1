package com.platform.domain.exception;

/** Thrown when a requested time slot is not available for booking. */
public class SlotUnavailableException extends RuntimeException {
    public SlotUnavailableException(String slotId) {
        super("Time slot is not available: " + slotId);
    }
}
