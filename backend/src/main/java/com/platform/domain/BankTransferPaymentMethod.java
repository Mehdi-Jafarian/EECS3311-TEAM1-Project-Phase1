package com.platform.domain;

import com.platform.domain.exception.InvalidPaymentException;

/**
 * Bank transfer payment method.
 *
 * <p>Validation rules:
 * <ul>
 *   <li>Account number: 8–17 digits (digits only).</li>
 *   <li>Routing number: exactly 9 digits (ABA format).</li>
 * </ul>
 */
public class BankTransferPaymentMethod extends PaymentMethod {

    private final String accountNumber;
    private final String routingNumber;

    public BankTransferPaymentMethod(String id, String clientId,
                                     String accountNumber, String routingNumber) {
        super(id, clientId, PaymentType.BANK_TRANSFER);
        this.accountNumber = accountNumber;
        this.routingNumber = routingNumber;
    }

    @Override
    public void validate() {
        if (accountNumber == null || !accountNumber.matches("\\d{8,17}")) {
            throw new InvalidPaymentException(
                    "Bank account number must be 8–17 digits.");
        }
        if (routingNumber == null || !routingNumber.matches("\\d{9}")) {
            throw new InvalidPaymentException(
                    "Bank routing number must be exactly 9 digits (ABA format).");
        }
    }

    public String getAccountNumber() { return accountNumber; }
    public String getRoutingNumber() { return routingNumber; }

    @Override
    public String toString() {
        String maskedAccount = accountNumber.length() > 4
                ? "****" + accountNumber.substring(accountNumber.length() - 4)
                : accountNumber;
        return "BankTransfer{id='" + getId() + "', account='" + maskedAccount
                + "', routing='" + routingNumber + "'}";
    }
}
