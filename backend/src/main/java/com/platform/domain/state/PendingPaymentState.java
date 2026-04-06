package com.platform.domain.state;

import com.platform.domain.Booking;
import com.platform.domain.BookingStatus;

/** State: PENDING_PAYMENT — client must pay; client can cancel. */
public class PendingPaymentState implements BookingStateHandler {

    @Override
    public void pay(Booking booking) {
        booking.setStateHandler(new PaidState());
    }

    @Override
    public void cancel(Booking booking) {
        booking.setStateHandler(new CancelledState());
    }

    @Override
    public BookingStatus getStatus() {
        return BookingStatus.PENDING_PAYMENT;
    }
}
