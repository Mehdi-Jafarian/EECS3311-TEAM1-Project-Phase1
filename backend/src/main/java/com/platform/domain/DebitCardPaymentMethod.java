package com.platform.domain;

import com.platform.domain.exception.InvalidPaymentException;

import java.time.YearMonth;

/**
 * Debit card payment method — same validation rules as credit card.
 *
 * <p>Validation rules:
 * <ul>
 *   <li>Card number: exactly 16 digits.</li>
 *   <li>Expiry: must be in the future.</li>
 *   <li>CVV: 3 or 4 digits.</li>
 * </ul>
 */
public class DebitCardPaymentMethod extends PaymentMethod {

    private final String cardNumber;
    private final int expiryMonth;
    private final int expiryYear;
    private final String cvv;

    public DebitCardPaymentMethod(String id, String clientId,
                                  String cardNumber, int expiryMonth,
                                  int expiryYear, String cvv) {
        super(id, clientId, PaymentType.DEBIT_CARD);
        this.cardNumber = cardNumber;
        this.expiryMonth = expiryMonth;
        this.expiryYear = expiryYear;
        this.cvv = cvv;
    }

    @Override
    public void validate() {
        if (cardNumber == null || !cardNumber.matches("\\d{16}")) {
            throw new InvalidPaymentException(
                    "Debit card number must be exactly 16 digits.");
        }
        YearMonth expiry = YearMonth.of(expiryYear, expiryMonth);
        if (expiry.isBefore(YearMonth.now())) {
            throw new InvalidPaymentException(
                    "Debit card has expired (expiry: " + expiryMonth + "/" + expiryYear + ").");
        }
        if (cvv == null || !cvv.matches("\\d{3,4}")) {
            throw new InvalidPaymentException(
                    "CVV must be 3 or 4 digits.");
        }
    }

    public String getCardNumber()  { return cardNumber; }
    public int    getExpiryMonth() { return expiryMonth; }
    public int    getExpiryYear()  { return expiryYear; }
    public String getCvv()         { return cvv; }

    @Override
    public String toString() {
        return String.format("DebitCard{id='%s', card='****%s', expiry=%02d/%d}",
                getId(), cardNumber.substring(12), expiryMonth, expiryYear);
    }
}
