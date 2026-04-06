package com.platform.domain.policy;

import com.platform.domain.ConsultingService;

/** Pricing strategy: return the service base price unchanged. */
public class BasePricingStrategy implements PricingStrategy {

    @Override
    public double calculatePrice(ConsultingService service) {
        return service.getBasePrice();
    }

    @Override
    public String getName() {
        return "Base Pricing (no discount)";
    }
}
