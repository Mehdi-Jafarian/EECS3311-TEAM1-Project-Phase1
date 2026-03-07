package com.platform.domain.policy;

import com.platform.domain.ConsultingService;

/** Pricing strategy: apply a fixed percentage discount to the base price. */
public class DiscountedPricingStrategy implements PricingStrategy {

    private final double discountPercent; // 0.0 – 1.0

    /**
     * @param discountPercent fraction to discount (e.g. 0.20 = 20 % off).
     */
    public DiscountedPricingStrategy(double discountPercent) {
        if (discountPercent < 0 || discountPercent > 1) {
            throw new IllegalArgumentException("discountPercent must be between 0.0 and 1.0");
        }
        this.discountPercent = discountPercent;
    }

    @Override
    public double calculatePrice(ConsultingService service) {
        return service.getBasePrice() * (1.0 - discountPercent);
    }

    @Override
    public String getName() {
        return String.format("Discounted Pricing (%.0f%% off)", discountPercent * 100);
    }
}
