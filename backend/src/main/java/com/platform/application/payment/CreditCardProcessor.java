package com.platform.application.payment;

import com.platform.domain.CreditCardPaymentMethod;
import com.platform.domain.PaymentMethod;
import com.platform.infrastructure.TimeProvider;

/** Concrete processor for credit card payments. */
public class CreditCardProcessor extends PaymentProcessor {

    public CreditCardProcessor(long delayMs, TimeProvider timeProvider) {
        super(delayMs, timeProvider);
    }

    @Override
    protected void validate(PaymentMethod method) {
        if (!(method instanceof CreditCardPaymentMethod)) {
            throw new com.platform.domain.exception.InvalidPaymentException(
                    "Expected a credit card payment method.");
        }
        method.validate();
    }
}
