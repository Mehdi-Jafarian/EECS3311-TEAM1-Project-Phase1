package com.platform.application.payment;

import com.platform.domain.PayPalPaymentMethod;
import com.platform.domain.PaymentMethod;
import com.platform.infrastructure.TimeProvider;

/** Concrete processor for PayPal payments. */
public class PayPalProcessor extends PaymentProcessor {

    public PayPalProcessor(long delayMs, TimeProvider timeProvider) {
        super(delayMs, timeProvider);
    }

    @Override
    protected void validate(PaymentMethod method) {
        if (!(method instanceof PayPalPaymentMethod)) {
            throw new com.platform.domain.exception.InvalidPaymentException(
                    "Expected a PayPal payment method.");
        }
        method.validate();
    }
}
