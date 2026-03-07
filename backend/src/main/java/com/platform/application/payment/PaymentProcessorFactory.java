package com.platform.application.payment;

import com.platform.domain.PaymentType;
import com.platform.infrastructure.TimeProvider;

/**
 * Factory Method Pattern — creates the appropriate {@link PaymentProcessor}
 * for a given {@link PaymentType}.
 *
 * <p>The {@code delayMs} parameter is injected so tests can pass 0 to skip delays.
 */
public class PaymentProcessorFactory {

    private final long delayMs;
    private final TimeProvider timeProvider;

    /** Production constructor — 2 500 ms simulated delay. */
    public PaymentProcessorFactory(TimeProvider timeProvider) {
        this(2500, timeProvider);
    }

    /** Test-friendly constructor — caller controls delay. */
    public PaymentProcessorFactory(long delayMs, TimeProvider timeProvider) {
        this.delayMs = delayMs;
        this.timeProvider = timeProvider;
    }

    /**
     * Creates a {@link PaymentProcessor} matching the given type.
     *
     * @param type the payment type
     * @return a concrete processor
     * @throws IllegalArgumentException for unknown types
     */
    public PaymentProcessor create(PaymentType type) {
        return switch (type) {
            case CREDIT_CARD   -> new CreditCardProcessor(delayMs, timeProvider);
            case DEBIT_CARD    -> new DebitCardProcessor(delayMs, timeProvider);
            case PAYPAL        -> new PayPalProcessor(delayMs, timeProvider);
            case BANK_TRANSFER -> new BankTransferProcessor(delayMs, timeProvider);
        };
    }
}
