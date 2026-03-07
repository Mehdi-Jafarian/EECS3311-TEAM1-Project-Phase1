package com.platform.domain.policy;

import com.platform.domain.Booking;

/**
 * Strategy Pattern — defines how a cancellation affects the refund amount.
 * Admin can swap implementations at runtime via {@code SystemPolicy}.
 */
public interface CancellationPolicy {

    /**
     * Calculates the refund amount given the original payment amount.
     *
     * @param booking the booking being cancelled
     * @param amount  the original amount paid
     * @return the refund amount (0.0 – amount)
     */
    double apply(Booking booking, double amount);

    /** Human-readable name for display in CLI/README. */
    String getName();
}
