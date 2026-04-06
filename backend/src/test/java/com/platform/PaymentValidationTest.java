package com.platform;

import com.platform.domain.*;
import com.platform.domain.exception.InvalidPaymentException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for all four payment method validation rules.
 */
@DisplayName("Payment Method Validation")
class PaymentValidationTest {

    private static final String CLIENT_ID = UUID.randomUUID().toString();

    // ── Credit Card ───────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Credit Card")
    class CreditCardTests {

        @Test
        void validCreditCard() {
            assertDoesNotThrow(() ->
                    new CreditCardPaymentMethod(id(), CLIENT_ID, "1234567890123456", 12, 2099, "123")
                            .validate());
        }

        @Test
        void cardNumberTooShort() {
            assertThrows(InvalidPaymentException.class, () ->
                    new CreditCardPaymentMethod(id(), CLIENT_ID, "123456789012345", 12, 2099, "123")
                            .validate());
        }

        @Test
        void cardNumberTooLong() {
            assertThrows(InvalidPaymentException.class, () ->
                    new CreditCardPaymentMethod(id(), CLIENT_ID, "12345678901234567", 12, 2099, "123")
                            .validate());
        }

        @Test
        void cardNumberContainsLetters() {
            assertThrows(InvalidPaymentException.class, () ->
                    new CreditCardPaymentMethod(id(), CLIENT_ID, "1234567890ABCDEF", 12, 2099, "123")
                            .validate());
        }

        @Test
        void expiredCard() {
            assertThrows(InvalidPaymentException.class, () ->
                    new CreditCardPaymentMethod(id(), CLIENT_ID, "1234567890123456", 1, 2020, "123")
                            .validate());
        }

        @Test
        void cvvTooShort() {
            assertThrows(InvalidPaymentException.class, () ->
                    new CreditCardPaymentMethod(id(), CLIENT_ID, "1234567890123456", 12, 2099, "12")
                            .validate());
        }

        @Test
        void cvvTooLong() {
            assertThrows(InvalidPaymentException.class, () ->
                    new CreditCardPaymentMethod(id(), CLIENT_ID, "1234567890123456", 12, 2099, "12345")
                            .validate());
        }

        @Test
        void cvvFourDigitsAccepted() {
            assertDoesNotThrow(() ->
                    new CreditCardPaymentMethod(id(), CLIENT_ID, "1234567890123456", 12, 2099, "1234")
                            .validate());
        }
    }

    // ── Debit Card ────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Debit Card")
    class DebitCardTests {

        @Test
        void validDebitCard() {
            assertDoesNotThrow(() ->
                    new DebitCardPaymentMethod(id(), CLIENT_ID, "9876543210987654", 6, 2030, "456")
                            .validate());
        }

        @Test
        void invalidDebitCardNumber() {
            assertThrows(InvalidPaymentException.class, () ->
                    new DebitCardPaymentMethod(id(), CLIENT_ID, "987654321098765", 6, 2030, "456")
                            .validate());
        }

        @Test
        void expiredDebitCard() {
            assertThrows(InvalidPaymentException.class, () ->
                    new DebitCardPaymentMethod(id(), CLIENT_ID, "9876543210987654", 1, 2019, "456")
                            .validate());
        }
    }

    // ── PayPal ────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("PayPal")
    class PayPalTests {

        @Test
        void validPayPalEmail() {
            assertDoesNotThrow(() ->
                    new PayPalPaymentMethod(id(), CLIENT_ID, "user@example.com").validate());
        }

        @Test
        void missingAtSign() {
            assertThrows(InvalidPaymentException.class, () ->
                    new PayPalPaymentMethod(id(), CLIENT_ID, "userexample.com").validate());
        }

        @Test
        void missingDomain() {
            assertThrows(InvalidPaymentException.class, () ->
                    new PayPalPaymentMethod(id(), CLIENT_ID, "user@").validate());
        }

        @Test
        void emptyEmail() {
            assertThrows(InvalidPaymentException.class, () ->
                    new PayPalPaymentMethod(id(), CLIENT_ID, "").validate());
        }

        @Test
        void nullEmail() {
            assertThrows(InvalidPaymentException.class, () ->
                    new PayPalPaymentMethod(id(), CLIENT_ID, null).validate());
        }

        @Test
        void subdomainEmailAccepted() {
            assertDoesNotThrow(() ->
                    new PayPalPaymentMethod(id(), CLIENT_ID, "user@mail.example.co.uk").validate());
        }
    }

    // ── Bank Transfer ─────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Bank Transfer")
    class BankTransferTests {

        @Test
        void validBankTransfer() {
            assertDoesNotThrow(() ->
                    new BankTransferPaymentMethod(id(), CLIENT_ID, "12345678", "123456789").validate());
        }

        @Test
        void accountNumberTooShort() {
            assertThrows(InvalidPaymentException.class, () ->
                    new BankTransferPaymentMethod(id(), CLIENT_ID, "1234567", "123456789").validate());
        }

        @Test
        void accountNumberTooLong() {
            assertThrows(InvalidPaymentException.class, () ->
                    new BankTransferPaymentMethod(id(), CLIENT_ID, "123456789012345678", "123456789")
                            .validate());
        }

        @Test
        void routingNumberTooShort() {
            assertThrows(InvalidPaymentException.class, () ->
                    new BankTransferPaymentMethod(id(), CLIENT_ID, "12345678", "12345678").validate());
        }

        @Test
        void routingNumberTooLong() {
            assertThrows(InvalidPaymentException.class, () ->
                    new BankTransferPaymentMethod(id(), CLIENT_ID, "12345678", "1234567890").validate());
        }

        @Test
        void routingNumberContainsLetters() {
            assertThrows(InvalidPaymentException.class, () ->
                    new BankTransferPaymentMethod(id(), CLIENT_ID, "12345678", "12345678A").validate());
        }

        @Test
        void maxLengthAccountAccepted() {
            assertDoesNotThrow(() ->
                    new BankTransferPaymentMethod(id(), CLIENT_ID, "12345678901234567", "123456789")
                            .validate());
        }
    }

    private static String id() { return UUID.randomUUID().toString(); }
}
