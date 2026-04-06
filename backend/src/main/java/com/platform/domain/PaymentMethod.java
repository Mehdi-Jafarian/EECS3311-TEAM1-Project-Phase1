package com.platform.domain;

import java.util.Objects;

/**
 * Abstract base for saved payment methods owned by a Client.
 * Subclasses implement {@link #validate()} with method-specific rules.
 */
public abstract class PaymentMethod {

    private final String id;
    private final String clientId;
    private final PaymentType paymentType;

    protected PaymentMethod(String id, String clientId, PaymentType paymentType) {
        this.id = Objects.requireNonNull(id, "id");
        this.clientId = Objects.requireNonNull(clientId, "clientId");
        this.paymentType = Objects.requireNonNull(paymentType, "paymentType");
    }

    public String      getId()          { return id; }
    public String      getClientId()    { return clientId; }
    public PaymentType getPaymentType() { return paymentType; }

    /**
     * Validates method-specific payment details.
     *
     * @throws com.platform.domain.exception.InvalidPaymentException if validation fails
     */
    public abstract void validate();

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{id='" + id + "', clientId='" + clientId + "'}";
    }
}
