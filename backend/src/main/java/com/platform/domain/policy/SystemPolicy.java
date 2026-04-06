package com.platform.domain.policy;

import java.util.Objects;

/**
 * Holds the system-wide configurable policies.
 * Admin uses {@link com.platform.application.AdminService} to update these at runtime.
 *
 * <p><b>Strategy Pattern (Context):</b> holds references to swappable
 * {@link CancellationPolicy} and {@link PricingStrategy} implementations.
 */
public class SystemPolicy {

    private CancellationPolicy cancellationPolicy;
    private PricingStrategy pricingStrategy;
    private boolean notificationsEnabled;

    public SystemPolicy() {
        this.cancellationPolicy = new FreeCancellationPolicy();
        this.pricingStrategy = new BasePricingStrategy();
        this.notificationsEnabled = true;
    }

    public CancellationPolicy getCancellationPolicy() {
        return cancellationPolicy;
    }

    public void setCancellationPolicy(CancellationPolicy cancellationPolicy) {
        this.cancellationPolicy = Objects.requireNonNull(cancellationPolicy);
    }

    public PricingStrategy getPricingStrategy() {
        return pricingStrategy;
    }

    public void setPricingStrategy(PricingStrategy pricingStrategy) {
        this.pricingStrategy = Objects.requireNonNull(pricingStrategy);
    }

    public boolean isNotificationsEnabled() {
        return notificationsEnabled;
    }

    public void setNotificationsEnabled(boolean notificationsEnabled) {
        this.notificationsEnabled = notificationsEnabled;
    }
}
