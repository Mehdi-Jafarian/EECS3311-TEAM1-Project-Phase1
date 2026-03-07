package com.platform.domain.state;

import com.platform.domain.Booking;
import com.platform.domain.BookingStatus;

/** State: PAID — consultant can mark booking complete; client can still cancel (triggers refund). */
public class PaidState implements BookingStateHandler {

    @Override
    public void complete(Booking booking) {
        booking.setStateHandler(new CompletedState());
    }

    @Override
    public void cancel(Booking booking) {
        booking.setStateHandler(new CancelledState());
    }

    @Override
    public BookingStatus getStatus() {
        return BookingStatus.PAID;
    }
}
