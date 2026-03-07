package com.platform.domain;

/** Represents the lifecycle state of a booking. */
public enum BookingStatus {
    REQUESTED,
    CONFIRMED,
    PENDING_PAYMENT,
    PAID,
    REJECTED,
    CANCELLED,
    COMPLETED
}
