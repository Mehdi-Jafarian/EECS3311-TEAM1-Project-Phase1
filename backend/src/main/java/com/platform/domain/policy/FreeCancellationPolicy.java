package com.platform.domain.policy;

import com.platform.domain.Booking;

/** Cancellation policy: full refund regardless of timing. */
public class FreeCancellationPolicy implements CancellationPolicy {

    @Override
    public double apply(Booking booking, double amount) {
        return amount; // 100 % refund
    }

    @Override
    public String getName() {
        return "Free Cancellation (100% refund)";
    }
}
