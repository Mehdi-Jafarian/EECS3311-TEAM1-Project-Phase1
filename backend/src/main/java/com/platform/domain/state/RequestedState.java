package com.platform.domain.state;

import com.platform.domain.Booking;
import com.platform.domain.BookingStatus;

/** State: REQUESTED — consultant can accept/reject; client can cancel. */
public class RequestedState implements BookingStateHandler {

    @Override
    public void accept(Booking booking) {
        booking.setStateHandler(new PendingPaymentState());
    }

    @Override
    public void reject(Booking booking) {
        booking.setStateHandler(new RejectedState());
    }

    @Override
    public void cancel(Booking booking) {
        booking.setStateHandler(new CancelledState());
    }

    @Override
    public BookingStatus getStatus() {
        return BookingStatus.REQUESTED;
    }
}
