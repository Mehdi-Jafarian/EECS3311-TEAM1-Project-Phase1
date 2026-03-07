package com.platform.application.payment;

import com.platform.domain.Payment;
import com.platform.domain.PaymentMethod;
import com.platform.infrastructure.TimeProvider;

import java.util.UUID;

/**
 * Template Method Pattern — defines the payment processing algorithm skeleton.
 *
 * <ol>
 *   <li>{@link #validate(PaymentMethod)} — method-specific validation (abstract).</li>
 *   <li>{@link #simulateDelay()} — simulates network/processing delay.</li>
 *   <li>{@link #generateTransactionId()} — produces a unique transaction ID.</li>
 *   <li>Creates and returns a {@link Payment} record.</li>
 * </ol>
 *
 * <p><b>Factory Method Pattern (Creator):</b> each subclass is also a concrete creator
 * paired with a specific {@link com.platform.domain.PaymentType}. The
 * {@link PaymentProcessorFactory} instantiates the correct subclass.
 */
public abstract class PaymentProcessor {

    /**
     * Processing delay in milliseconds.
     * Set to a lower value in tests to keep them fast.
     */
    protected long delayMs;
    protected final TimeProvider timeProvider;

    protected PaymentProcessor(long delayMs, TimeProvider timeProvider) {
        this.delayMs = delayMs;
        this.timeProvider = timeProvider;
    }

    /**
     * Processes a payment.
     *
     * @param method    the payment method to charge
     * @param amount    the amount to charge
     * @param bookingId the booking this payment is for
     * @param clientId  the paying client
     * @return a committed {@link Payment} record
     */
    public final Payment process(PaymentMethod method, double amount,
                                  String bookingId, String clientId) {
        validate(method);
        simulateDelay();
        String transactionId = generateTransactionId();
        return new Payment(
                UUID.randomUUID().toString(),
                bookingId,
                clientId,
                amount,
                method.getPaymentType(),
                transactionId,
                timeProvider.now()
        );
    }

    /** Method-specific validation — implemented by each concrete processor. */
    protected abstract void validate(PaymentMethod method);

    /** Simulates network/processor delay. */
    protected void simulateDelay() {
        if (delayMs > 0) {
            try {
                Thread.sleep(delayMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /** Generates a unique transaction ID (UUID). */
    protected String generateTransactionId() {
        return "TXN-" + UUID.randomUUID().toString().toUpperCase();
    }

    public void setDelayMs(long delayMs) {
        this.delayMs = delayMs;
    }
}
