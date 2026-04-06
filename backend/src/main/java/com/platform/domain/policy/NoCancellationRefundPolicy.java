package com.platform.domain.policy;

import com.platform.domain.Booking;

/** Cancellation policy: no refund. */
public class NoCancellationRefundPolicy implements CancellationPolicy {

    @Override
    public double apply(Booking booking, double amount) {
        return 0.0; // no refund
    }

    @Override
    public String getName() {
        return "No Refund Policy";
    }
}
