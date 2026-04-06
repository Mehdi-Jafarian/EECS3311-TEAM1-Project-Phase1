package com.platform;

import com.platform.domain.*;
import com.platform.domain.exception.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Booking lifecycle state transitions (valid and invalid).
 */
@DisplayName("Booking State Transitions")
class BookingStateTransitionTest {

    private TestFixtures.Services svc;
    private TestFixtures.SeedResult seed;

    @BeforeEach
    void setup() {
        svc  = TestFixtures.buildServices();
        seed = TestFixtures.seed(svc);
    }

    @Test
    @DisplayName("New booking starts in REQUESTED state")
    void newBookingIsRequested() {
        Booking b = svc.booking().requestBooking(
                seed.client().getId(), seed.consultant().getId(),
                seed.service().getId(), seed.slot().getId());
        assertEquals(BookingStatus.REQUESTED, b.getStatus());
    }

    @Test
    @DisplayName("Consultant accepting REQUESTED → PENDING_PAYMENT")
    void acceptMovesToPendingPayment() {
        Booking b = requestBooking();
        svc.booking().acceptBooking(b.getId(), seed.consultant().getId());
        assertEquals(BookingStatus.PENDING_PAYMENT, b.getStatus());
    }

    @Test
    @DisplayName("Consultant rejecting REQUESTED → REJECTED")
    void rejectMovesToRejected() {
        Booking b = requestBooking();
        svc.booking().rejectBooking(b.getId(), seed.consultant().getId());
        assertEquals(BookingStatus.REJECTED, b.getStatus());
    }

    @Test
    @DisplayName("Cannot pay for a REQUESTED (not-yet-accepted) booking")
    void cannotPayRequestedBooking() {
        Booking b = requestBooking();
        assertThrows(InvalidBookingStateException.class,
                () -> svc.booking().markAsPaid(b.getId()));
    }

    @Test
    @DisplayName("PENDING_PAYMENT → PAID after payment")
    void payMovesToPaid() {
        Booking b = requestBooking();
        svc.booking().acceptBooking(b.getId(), seed.consultant().getId());
        svc.booking().markAsPaid(b.getId());
        assertEquals(BookingStatus.PAID, b.getStatus());
    }

    @Test
    @DisplayName("PAID → COMPLETED when consultant completes")
    void completeMovesToCompleted() {
        Booking b = requestAndPay();
        svc.booking().completeBooking(b.getId(), seed.consultant().getId());
        assertEquals(BookingStatus.COMPLETED, b.getStatus());
    }

    @Test
    @DisplayName("Cannot complete a booking that has not been paid")
    void cannotCompleteUnpaidBooking() {
        Booking b = requestBooking();
        svc.booking().acceptBooking(b.getId(), seed.consultant().getId());
        assertThrows(InvalidBookingStateException.class,
                () -> svc.booking().completeBooking(b.getId(), seed.consultant().getId()));
    }

    @Test
    @DisplayName("Client can cancel a REQUESTED booking")
    void cancelRequestedBooking() {
        Booking b = requestBooking();
        svc.booking().cancelBooking(b.getId(), seed.client().getId());
        assertEquals(BookingStatus.CANCELLED, b.getStatus());
    }

    @Test
    @DisplayName("Client can cancel a PENDING_PAYMENT booking")
    void cancelPendingPaymentBooking() {
        Booking b = requestBooking();
        svc.booking().acceptBooking(b.getId(), seed.consultant().getId());
        svc.booking().cancelBooking(b.getId(), seed.client().getId());
        assertEquals(BookingStatus.CANCELLED, b.getStatus());
    }

    @Test
    @DisplayName("Cannot cancel a COMPLETED booking")
    void cannotCancelCompletedBooking() {
        Booking b = requestAndPay();
        svc.booking().completeBooking(b.getId(), seed.consultant().getId());
        assertThrows(InvalidBookingStateException.class,
                () -> svc.booking().cancelBooking(b.getId(), seed.client().getId()));
    }

    @Test
    @DisplayName("Cannot accept a REJECTED booking")
    void cannotAcceptRejectedBooking() {
        Booking b = requestBooking();
        svc.booking().rejectBooking(b.getId(), seed.consultant().getId());
        assertThrows(InvalidBookingStateException.class,
                () -> svc.booking().acceptBooking(b.getId(), seed.consultant().getId()));
    }

    @Test
    @DisplayName("Unauthorized client cannot cancel another client's booking")
    void unauthorizedCancelThrows() {
        Booking b = requestBooking();
        Client other = svc.client().registerClient("Other", "other@test.com");
        assertThrows(UnauthorizedActionException.class,
                () -> svc.booking().cancelBooking(b.getId(), other.getId()));
    }

    @Test
    @DisplayName("Unapproved consultant cannot receive bookings")
    void unapprovedConsultantThrows() {
        var pendingConsultant = svc.consultant().registerConsultant("Pending", "pending@test.com");
        assertThrows(ConsultantNotApprovedException.class,
                () -> svc.booking().requestBooking(
                        seed.client().getId(), pendingConsultant.getId(),
                        seed.service().getId(), seed.slot().getId()));
    }

    @Test
    @DisplayName("Double-booking same slot throws SlotUnavailableException")
    void doubleBookingThrows() {
        requestBooking(); // slot is now booked
        Client other = svc.client().registerClient("Other", "other@test.com");
        assertThrows(SlotUnavailableException.class,
                () -> svc.booking().requestBooking(
                        other.getId(), seed.consultant().getId(),
                        seed.service().getId(), seed.slot().getId()));
    }

    @Test
    @DisplayName("Slot is released when booking is rejected")
    void slotReleasedOnReject() {
        Booking b = requestBooking();
        assertFalse(seed.slot().isAvailable());
        svc.booking().rejectBooking(b.getId(), seed.consultant().getId());
        // slot should be available again
        var slot = svc.consultant().getTimeSlots(seed.consultant().getId()).stream()
                .filter(s -> s.getId().equals(seed.slot().getId())).findFirst().orElseThrow();
        assertTrue(slot.isAvailable());
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private Booking requestBooking() {
        return svc.booking().requestBooking(
                seed.client().getId(), seed.consultant().getId(),
                seed.service().getId(), seed.slot().getId());
    }

    private Booking requestAndPay() {
        Booking b = requestBooking();
        svc.booking().acceptBooking(b.getId(), seed.consultant().getId());
        svc.booking().markAsPaid(b.getId());
        return b;
    }
}
