package com.platform.domain.state;

import com.platform.domain.Booking;
import com.platform.domain.BookingStatus;

/**
 * State Pattern — Context interface.
 * Each concrete state handles valid transitions and throws
 * {@link com.platform.domain.exception.InvalidBookingStateException}
 * for invalid ones.
 */
public interface BookingStateHandler {

    /** Transition to CONFIRMED (not used in the simplified flow but available). */
    default void confirm(Booking booking) {
        throw new com.platform.domain.exception.InvalidBookingStateException(
                "Cannot confirm booking in state: " + getStatus());
    }

    /** Consultant accepts → booking moves to PENDING_PAYMENT. */
    default void accept(Booking booking) {
        throw new com.platform.domain.exception.InvalidBookingStateException(
                "Cannot accept booking in state: " + getStatus());
    }

    /** Consultant rejects → booking moves to REJECTED. */
    default void reject(Booking booking) {
        throw new com.platform.domain.exception.InvalidBookingStateException(
                "Cannot reject booking in state: " + getStatus());
    }

    /** Client pays → booking moves to PAID. */
    default void pay(Booking booking) {
        throw new com.platform.domain.exception.InvalidBookingStateException(
                "Cannot pay for booking in state: " + getStatus());
    }

    /** Consultant marks complete → booking moves to COMPLETED. */
    default void complete(Booking booking) {
        throw new com.platform.domain.exception.InvalidBookingStateException(
                "Cannot complete booking in state: " + getStatus());
    }

    /** Client or system cancels → booking moves to CANCELLED. */
    default void cancel(Booking booking) {
        throw new com.platform.domain.exception.InvalidBookingStateException(
                "Cannot cancel booking in state: " + getStatus());
    }

    /** Returns the {@link BookingStatus} this handler represents. */
    BookingStatus getStatus();
}
