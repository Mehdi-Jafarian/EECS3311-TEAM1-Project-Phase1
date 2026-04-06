package com.platform.domain.state;

import com.platform.domain.Booking;
import com.platform.domain.BookingStatus;

/** State: COMPLETED — terminal state; no further transitions allowed. */
public class CompletedState implements BookingStateHandler {

    @Override
    public BookingStatus getStatus() {
        return BookingStatus.COMPLETED;
    }
}
