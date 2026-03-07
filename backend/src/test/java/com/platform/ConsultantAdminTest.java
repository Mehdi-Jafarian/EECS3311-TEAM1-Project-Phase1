package com.platform;

import com.platform.domain.*;
import com.platform.domain.exception.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for consultant accept/reject behavior (UC9) and admin approval (UC11).
 */
@DisplayName("Consultant & Admin Behavior")
class ConsultantAdminTest {

    private TestFixtures.Services svc;
    private TestFixtures.SeedResult seed;

    @BeforeEach
    void setup() {
        svc  = TestFixtures.buildServices();
        seed = TestFixtures.seed(svc);
    }

    // ── UC9 accept/reject ─────────────────────────────────────────────────────

    @Test
    @DisplayName("Accepted booking moves to PENDING_PAYMENT")
    void acceptMovesToPendingPayment() {
        Booking b = requestBooking();
        svc.booking().acceptBooking(b.getId(), seed.consultant().getId());
        assertEquals(BookingStatus.PENDING_PAYMENT, b.getStatus());
    }

    @Test
    @DisplayName("Rejected booking moves to REJECTED")
    void rejectMovesToRejected() {
        Booking b = requestBooking();
        svc.booking().rejectBooking(b.getId(), seed.consultant().getId());
        assertEquals(BookingStatus.REJECTED, b.getStatus());
    }

    @Test
    @DisplayName("Wrong consultant cannot accept a booking")
    void wrongConsultantCannotAccept() {
        Booking b = requestBooking();
        var other = svc.consultant().registerConsultant("Other", "other@test.com");
        svc.admin().approveConsultant(other.getId());
        assertThrows(UnauthorizedActionException.class,
                () -> svc.booking().acceptBooking(b.getId(), other.getId()));
    }

    @Test
    @DisplayName("Wrong consultant cannot reject a booking")
    void wrongConsultantCannotReject() {
        Booking b = requestBooking();
        var other = svc.consultant().registerConsultant("Other2", "other2@test.com");
        svc.admin().approveConsultant(other.getId());
        assertThrows(UnauthorizedActionException.class,
                () -> svc.booking().rejectBooking(b.getId(), other.getId()));
    }

    @Test
    @DisplayName("Notification is sent to client when booking is accepted")
    void notificationOnAccept() {
        Booking b = requestBooking();
        svc.booking().acceptBooking(b.getId(), seed.consultant().getId());
        var notifs = svc.notification().getNotifications(seed.client().getId());
        assertTrue(notifs.stream().anyMatch(n -> n.getMessage().contains("accepted")));
    }

    @Test
    @DisplayName("Notification is sent to client when booking is rejected")
    void notificationOnReject() {
        Booking b = requestBooking();
        svc.booking().rejectBooking(b.getId(), seed.consultant().getId());
        var notifs = svc.notification().getNotifications(seed.client().getId());
        assertTrue(notifs.stream().anyMatch(n -> n.getMessage().contains("rejected")));
    }

    // ── UC11 admin approval ───────────────────────────────────────────────────

    @Test
    @DisplayName("New consultant starts in PENDING status")
    void newConsultantIsPending() {
        var consultant = svc.consultant().registerConsultant("New", "new@test.com");
        assertEquals(ConsultantStatus.PENDING, consultant.getStatus());
    }

    @Test
    @DisplayName("Admin can approve a PENDING consultant")
    void adminApprovesConsultant() {
        var c = svc.consultant().registerConsultant("Pending", "pending@test.com");
        svc.admin().approveConsultant(c.getId());
        assertEquals(ConsultantStatus.APPROVED, c.getStatus());
    }

    @Test
    @DisplayName("Admin can reject a PENDING consultant")
    void adminRejectsConsultant() {
        var c = svc.consultant().registerConsultant("Pending2", "pending2@test.com");
        svc.admin().rejectConsultant(c.getId());
        assertEquals(ConsultantStatus.REJECTED, c.getStatus());
    }

    @Test
    @DisplayName("Approving non-existent consultant throws EntityNotFoundException")
    void approveNonExistentConsultant() {
        assertThrows(EntityNotFoundException.class,
                () -> svc.admin().approveConsultant("nonexistent-id"));
    }

    @Test
    @DisplayName("Notification is sent to consultant on approval")
    void notificationOnApproval() {
        var c = svc.consultant().registerConsultant("Notified", "notified@test.com");
        svc.admin().approveConsultant(c.getId());
        var notifs = svc.notification().getNotifications(c.getId());
        assertFalse(notifs.isEmpty());
        assertTrue(notifs.stream().anyMatch(n -> n.getMessage().contains("approved")));
    }

    @Test
    @DisplayName("Approved consultant can add time slots")
    void approvedConsultantAddsSlots() {
        var c = svc.consultant().registerConsultant("Slot Adder", "slots@test.com");
        svc.admin().approveConsultant(c.getId());
        var slot = TestFixtures.futureSlot(c.getId());
        assertDoesNotThrow(() -> svc.consultant().addTimeSlot(c.getId(), slot));
    }

    @Test
    @DisplayName("Unapproved consultant cannot add time slots")
    void unapprovedConsultantCannotAddSlots() {
        var c = svc.consultant().registerConsultant("No Slots", "noslots@test.com");
        var slot = TestFixtures.futureSlot(c.getId());
        assertThrows(ConsultantNotApprovedException.class,
                () -> svc.consultant().addTimeSlot(c.getId(), slot));
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private Booking requestBooking() {
        return svc.booking().requestBooking(
                seed.client().getId(), seed.consultant().getId(),
                seed.service().getId(), seed.slot().getId());
    }
}
