package com.platform.domain.state;

import com.platform.domain.Booking;
import com.platform.domain.BookingStatus;

/** State: REJECTED — terminal state; no further transitions allowed. */
public class RejectedState implements BookingStateHandler {

    @Override
    public BookingStatus getStatus() {
        return BookingStatus.REJECTED;
    }
}
