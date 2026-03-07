package com.platform.application.payment;

import com.platform.domain.DebitCardPaymentMethod;
import com.platform.domain.PaymentMethod;
import com.platform.infrastructure.TimeProvider;

/** Concrete processor for debit card payments. */
public class DebitCardProcessor extends PaymentProcessor {

    public DebitCardProcessor(long delayMs, TimeProvider timeProvider) {
        super(delayMs, timeProvider);
    }

    @Override
    protected void validate(PaymentMethod method) {
        if (!(method instanceof DebitCardPaymentMethod)) {
            throw new com.platform.domain.exception.InvalidPaymentException(
                    "Expected a debit card payment method.");
        }
        method.validate();
    }
}
