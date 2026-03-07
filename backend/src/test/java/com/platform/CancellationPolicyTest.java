package com.platform;

import com.platform.domain.*;
import com.platform.domain.exception.InvalidBookingStateException;
import com.platform.domain.policy.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for cancellation policy behavior and admin policy configuration.
 */
@DisplayName("Cancellation Policy & Admin Policy")
class CancellationPolicyTest {

    private TestFixtures.Services svc;
    private TestFixtures.SeedResult seed;

    @BeforeEach
    void setup() {
        svc  = TestFixtures.buildServices();
        seed = TestFixtures.seed(svc);
    }

    // ── Policy unit tests ─────────────────────────────────────────────────────

    @Test
    @DisplayName("FreeCancellationPolicy returns full amount")
    void freePolicyFullRefund() {
        CancellationPolicy policy = new FreeCancellationPolicy();
        Booking b = dummyBooking();
        assertEquals(100.0, policy.apply(b, 100.0), 0.001);
        assertEquals(0.0,   policy.apply(b, 0.0), 0.001);
    }

    @Test
    @DisplayName("PartialRefundPolicy returns configured percentage")
    void partialRefund50Percent() {
        CancellationPolicy policy = new PartialRefundPolicy(0.5);
        Booking b = dummyBooking();
        assertEquals(50.0, policy.apply(b, 100.0), 0.001);
        assertEquals(25.0, policy.apply(b, 50.0),  0.001);
    }

    @Test
    @DisplayName("NoCancellationRefundPolicy returns zero")
    void noPolicyZeroRefund() {
        CancellationPolicy policy = new NoCancellationRefundPolicy();
        Booking b = dummyBooking();
        assertEquals(0.0, policy.apply(b, 100.0), 0.001);
    }

    @Test
    @DisplayName("PartialRefundPolicy rejects invalid percent")
    void partialRefundInvalidPercent() {
        assertThrows(IllegalArgumentException.class, () -> new PartialRefundPolicy(1.5));
        assertThrows(IllegalArgumentException.class, () -> new PartialRefundPolicy(-0.1));
    }

    // ── Admin can change policy and it affects cancellation refund ─────────────

    @Test
    @DisplayName("Admin sets NoRefund policy — cancellation yields no refund")
    void adminSetsNoRefundPolicy() {
        svc.admin().setCancellationPolicy(new NoCancellationRefundPolicy());

        Booking b = requestAndPay(150.0);
        double refund = svc.booking().cancelBooking(b.getId(), seed.client().getId());
        assertEquals(0.0, refund, 0.001);
    }

    @Test
    @DisplayName("Admin sets Free policy — cancellation returns full amount paid")
    void adminSetsFreePolicy() {
        svc.admin().setCancellationPolicy(new FreeCancellationPolicy());

        Booking b = requestAndPay(150.0);
        double refund = svc.booking().cancelBooking(b.getId(), seed.client().getId());
        assertEquals(150.0, refund, 0.001);
    }

    @Test
    @DisplayName("Admin sets 50% Partial policy — cancellation returns half")
    void adminSetsPartialPolicy() {
        svc.admin().setCancellationPolicy(new PartialRefundPolicy(0.5));

        Booking b = requestAndPay(200.0);
        double refund = svc.booking().cancelBooking(b.getId(), seed.client().getId());
        assertEquals(100.0, refund, 0.001);
    }

    @Test
    @DisplayName("Cancelling an unpaid booking returns 0 refund regardless of policy")
    void cancellingUnpaidBookingReturnsZero() {
        svc.admin().setCancellationPolicy(new FreeCancellationPolicy());

        Booking b = svc.booking().requestBooking(
                seed.client().getId(), seed.consultant().getId(),
                seed.service().getId(), seed.slot().getId());
        double refund = svc.booking().cancelBooking(b.getId(), seed.client().getId());
        assertEquals(0.0, refund, 0.001);
    }

    // ── Pricing strategy tests ────────────────────────────────────────────────

    @Test
    @DisplayName("BasePricingStrategy returns service base price")
    void basePricingReturnsBasePrice() {
        PricingStrategy strategy = new BasePricingStrategy();
        ConsultingService srv = TestFixtures.service("Test", 200.0);
        assertEquals(200.0, strategy.calculatePrice(srv), 0.001);
    }

    @Test
    @DisplayName("DiscountedPricingStrategy reduces price by configured percent")
    void discountedPricingReducesPrice() {
        PricingStrategy strategy = new DiscountedPricingStrategy(0.20); // 20% off
        ConsultingService srv = TestFixtures.service("Test", 100.0);
        assertEquals(80.0, strategy.calculatePrice(srv), 0.001);
    }

    @Test
    @DisplayName("Admin switches to discounted pricing — service price reflects discount")
    void adminSetsDiscountedPricing() {
        svc.admin().setPricingStrategy(new DiscountedPricingStrategy(0.10)); // 10% off
        double price = svc.catalog().getPriceFor(seed.service().getId());
        assertEquals(90.0, price, 0.001); // 100 * 0.90
    }

    // ── Notification toggle ───────────────────────────────────────────────────

    @Test
    @DisplayName("Disabling notifications suppresses event dispatch")
    void disableNotifications() {
        svc.admin().setNotificationsEnabled(false);
        assertFalse(svc.policy().isNotificationsEnabled());
        // Request booking — no notification should be created
        Booking b = svc.booking().requestBooking(
                seed.client().getId(), seed.consultant().getId(),
                seed.service().getId(), seed.slot().getId());
        var notifs = svc.notification().getNotifications(seed.client().getId());
        // Only approval notification was sent (before disabling); nothing after
        assertTrue(notifs.stream().noneMatch(n -> n.getMessage().contains("booking request")));
    }

    @Test
    @DisplayName("Enabling notifications resumes event dispatch")
    void enableNotifications() {
        svc.admin().setNotificationsEnabled(false);
        svc.admin().setNotificationsEnabled(true);
        assertTrue(svc.policy().isNotificationsEnabled());
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Booking requestAndPay(double amount) {
        Booking b = svc.booking().requestBooking(
                seed.client().getId(), seed.consultant().getId(),
                seed.service().getId(), seed.slot().getId());
        svc.booking().acceptBooking(b.getId(), seed.consultant().getId());
        String pmId = UUID.randomUUID().toString();
        svc.payment().addPaymentMethod(
                new CreditCardPaymentMethod(pmId, seed.client().getId(),
                        "1234567890123456", 12, 2099, "123"));
        svc.payment().processPayment(b.getId(), pmId, amount);
        return b;
    }

    private Booking dummyBooking() {
        return new Booking(UUID.randomUUID().toString(), "c1", "co1", "s1", "sl1",
                TestFixtures.FIXED_NOW);
    }
}
