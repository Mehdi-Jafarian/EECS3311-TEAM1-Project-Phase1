package com.platform;

import com.platform.domain.*;
import com.platform.domain.exception.InvalidBookingStateException;
import com.platform.domain.exception.InvalidPaymentException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the full payment flow: process payment → generate transaction → booking PAID.
 */
@DisplayName("Payment Flow")
class PaymentFlowTest {

    private TestFixtures.Services svc;
    private TestFixtures.SeedResult seed;

    @BeforeEach
    void setup() {
        svc  = TestFixtures.buildServices();
        seed = TestFixtures.seed(svc);
    }

    @Test
    @DisplayName("Successful payment creates a Payment record and moves booking to PAID")
    void successfulPayment() {
        // Arrange — request + accept
        Booking booking = svc.booking().requestBooking(
                seed.client().getId(), seed.consultant().getId(),
                seed.service().getId(), seed.slot().getId());
        svc.booking().acceptBooking(booking.getId(), seed.consultant().getId());

        // Add a valid credit card
        String pmId = UUID.randomUUID().toString();
        var cc = new CreditCardPaymentMethod(pmId, seed.client().getId(),
                "1234567890123456", 12, 2099, "123");
        svc.payment().addPaymentMethod(cc);

        // Act
        Payment payment = svc.payment().processPayment(booking.getId(), pmId, 100.0);

        // Assert payment record
        assertNotNull(payment);
        assertNotNull(payment.getTransactionId());
        assertTrue(payment.getTransactionId().startsWith("TXN-"));
        assertEquals("SUCCESS", payment.getStatus());
        assertEquals(100.0, payment.getAmount(), 0.001);
        assertEquals(booking.getId(), payment.getBookingId());
        assertEquals(seed.client().getId(), payment.getClientId());

        // Assert booking is now PAID
        assertEquals(BookingStatus.PAID, booking.getStatus());
    }

    @Test
    @DisplayName("Transaction ID is unique across multiple payments")
    void transactionIdIsUnique() {
        // Pay for booking 1
        Booking b1 = createAndAcceptBooking();
        String pmId = addCreditCard(seed.client().getId());
        Payment p1 = svc.payment().processPayment(b1.getId(), pmId, 100.0);

        // Create a second slot and booking
        var slot2 = TestFixtures.futureSlot(seed.consultant().getId());
        svc.consultant().addTimeSlot(seed.consultant().getId(), slot2);
        Booking b2 = svc.booking().requestBooking(
                seed.client().getId(), seed.consultant().getId(),
                seed.service().getId(), slot2.getId());
        svc.booking().acceptBooking(b2.getId(), seed.consultant().getId());
        Payment p2 = svc.payment().processPayment(b2.getId(), pmId, 100.0);

        assertNotEquals(p1.getTransactionId(), p2.getTransactionId());
    }

    @Test
    @DisplayName("Cannot pay for booking in REQUESTED state")
    void cannotPayRequestedBooking() {
        Booking b = svc.booking().requestBooking(
                seed.client().getId(), seed.consultant().getId(),
                seed.service().getId(), seed.slot().getId());
        String pmId = addCreditCard(seed.client().getId());

        assertThrows(InvalidBookingStateException.class,
                () -> svc.payment().processPayment(b.getId(), pmId, 100.0));
    }

    @Test
    @DisplayName("Paying with invalid payment method throws InvalidPaymentException")
    void invalidPaymentMethodThrows() {
        Booking b = createAndAcceptBooking();
        // Add an expired card
        String pmId = UUID.randomUUID().toString();
        var expiredCard = new CreditCardPaymentMethod(pmId, seed.client().getId(),
                "1234567890123456", 1, 2020, "123");
        // addPaymentMethod validates — should fail
        assertThrows(InvalidPaymentException.class,
                () -> svc.payment().addPaymentMethod(expiredCard));
    }

    @Test
    @DisplayName("Payment history contains the payment after processing")
    void paymentHistoryContainsPayment() {
        Booking b = createAndAcceptBooking();
        String pmId = addCreditCard(seed.client().getId());
        Payment payment = svc.payment().processPayment(b.getId(), pmId, 150.0);

        List<Payment> history = svc.payment().getPaymentHistory(seed.client().getId());
        assertTrue(history.stream().anyMatch(p -> p.getId().equals(payment.getId())));
    }

    @Test
    @DisplayName("PayPal payment succeeds with valid email")
    void payPalPaymentSucceeds() {
        Booking b = createAndAcceptBooking();
        String pmId = UUID.randomUUID().toString();
        var paypal = new PayPalPaymentMethod(pmId, seed.client().getId(), "user@example.com");
        svc.payment().addPaymentMethod(paypal);

        Payment payment = svc.payment().processPayment(b.getId(), pmId, 50.0);
        assertEquals(BookingStatus.PAID, b.getStatus());
        assertNotNull(payment.getTransactionId());
    }

    @Test
    @DisplayName("Bank transfer payment succeeds with valid details")
    void bankTransferPaymentSucceeds() {
        Booking b = createAndAcceptBooking();
        String pmId = UUID.randomUUID().toString();
        var bank = new BankTransferPaymentMethod(pmId, seed.client().getId(),
                "12345678", "123456789");
        svc.payment().addPaymentMethod(bank);

        Payment payment = svc.payment().processPayment(b.getId(), pmId, 200.0);
        assertEquals(BookingStatus.PAID, b.getStatus());
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Booking createAndAcceptBooking() {
        Booking b = svc.booking().requestBooking(
                seed.client().getId(), seed.consultant().getId(),
                seed.service().getId(), seed.slot().getId());
        svc.booking().acceptBooking(b.getId(), seed.consultant().getId());
        return b;
    }

    private String addCreditCard(String clientId) {
        String pmId = UUID.randomUUID().toString();
        var cc = new CreditCardPaymentMethod(pmId, clientId,
                "1234567890123456", 12, 2099, "123");
        svc.payment().addPaymentMethod(cc);
        return pmId;
    }
}
