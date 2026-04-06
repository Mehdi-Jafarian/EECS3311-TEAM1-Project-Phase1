package com.platform.domain.policy;

import com.platform.domain.ConsultingService;

/**
 * Strategy Pattern — defines how the price of a service is calculated.
 * Admin can swap implementations at runtime via {@code SystemPolicy}.
 */
public interface PricingStrategy {

    /**
     * Calculates the effective price for the given service.
     *
     * @param service the consulting service
     * @return calculated price in platform currency units
     */
    double calculatePrice(ConsultingService service);

    /** Human-readable name for display. */
    String getName();
}
