package com.platform.domain.policy;

import com.platform.domain.Booking;

/** Cancellation policy: partial refund — configurable percentage returned. */
public class PartialRefundPolicy implements CancellationPolicy {

    private final double refundPercent; // 0.0 – 1.0

    /**
     * @param refundPercent fraction of the paid amount to refund (e.g. 0.5 = 50 %).
     */
    public PartialRefundPolicy(double refundPercent) {
        if (refundPercent < 0 || refundPercent > 1) {
            throw new IllegalArgumentException("refundPercent must be between 0.0 and 1.0");
        }
        this.refundPercent = refundPercent;
    }

    @Override
    public double apply(Booking booking, double amount) {
        return amount * refundPercent;
    }

    @Override
    public String getName() {
        return String.format("Partial Refund (%.0f%%)", refundPercent * 100);
    }
}
