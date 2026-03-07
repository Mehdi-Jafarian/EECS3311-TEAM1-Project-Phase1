package com.platform.domain.state;

import com.platform.domain.Booking;
import com.platform.domain.BookingStatus;

/** State: CANCELLED — terminal state; no further transitions allowed. */
public class CancelledState implements BookingStateHandler {

    @Override
    public BookingStatus getStatus() {
        return BookingStatus.CANCELLED;
    }
}
