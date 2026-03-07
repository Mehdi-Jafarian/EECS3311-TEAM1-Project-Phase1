package com.platform.domain;

import com.platform.domain.exception.InvalidPaymentException;

/**
 * PayPal payment method.
 *
 * <p>Validation rules:
 * <ul>
 *   <li>Email must match standard email format: {@code local@domain.tld}</li>
 * </ul>
 */
public class PayPalPaymentMethod extends PaymentMethod {

    private final String email;

    public PayPalPaymentMethod(String id, String clientId, String email) {
        super(id, clientId, PaymentType.PAYPAL);
        this.email = email;
    }

    @Override
    public void validate() {
        if (email == null || !email.matches("^[\\w.+\\-]+@[\\w\\-]+(\\.[\\w\\-]+)+$")) {
            throw new InvalidPaymentException(
                    "PayPal email is not a valid email address: " + email);
        }
    }

    public String getEmail() { return email; }

    @Override
    public String toString() {
        return "PayPal{id='" + getId() + "', email='" + email + "'}";
    }
}
