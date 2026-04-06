package com.platform.application.payment;

import com.platform.domain.BankTransferPaymentMethod;
import com.platform.domain.PaymentMethod;
import com.platform.infrastructure.TimeProvider;

/** Concrete processor for bank transfer payments. */
public class BankTransferProcessor extends PaymentProcessor {

    public BankTransferProcessor(long delayMs, TimeProvider timeProvider) {
        super(delayMs, timeProvider);
    }

    @Override
    protected void validate(PaymentMethod method) {
        if (!(method instanceof BankTransferPaymentMethod)) {
            throw new com.platform.domain.exception.InvalidPaymentException(
                    "Expected a bank transfer payment method.");
        }
        method.validate();
    }
}
