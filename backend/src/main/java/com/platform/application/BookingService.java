package com.platform.application;

import com.platform.domain.*;
import com.platform.domain.exception.*;
import com.platform.domain.policy.SystemPolicy;
import com.platform.infrastructure.TimeProvider;
import com.platform.infrastructure.repository.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Application service for booking-related use cases (UC2, UC3, UC4, UC9, UC10).
 *
 * <p><b>Observer Pattern (Subject):</b> notifies registered {@link BookingEventObserver}s
 * whenever a booking lifecycle event occurs.
 */
public class BookingService {

    private final BookingRepository bookingRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final ServiceRepository serviceRepository;
    private final ConsultantRepository consultantRepository;
    private final ClientRepository clientRepository;
    private final PaymentRepository paymentRepository;
    private final SystemPolicy systemPolicy;
    private final TimeProvider timeProvider;
    private final List<BookingEventObserver> observers = new ArrayList<>();

    public BookingService(BookingRepository bookingRepository,
                          TimeSlotRepository timeSlotRepository,
                          ServiceRepository serviceRepository,
                          ConsultantRepository consultantRepository,
                          ClientRepository clientRepository,
                          PaymentRepository paymentRepository,
                          SystemPolicy systemPolicy,
                          TimeProvider timeProvider) {
        this.bookingRepository = bookingRepository;
        this.timeSlotRepository = timeSlotRepository;
        this.serviceRepository = serviceRepository;
        this.consultantRepository = consultantRepository;
        this.clientRepository = clientRepository;
        this.paymentRepository = paymentRepository;
        this.systemPolicy = systemPolicy;
        this.timeProvider = timeProvider;
    }

    /** Register an observer to receive booking lifecycle events. */
    public void addObserver(BookingEventObserver observer) {
        observers.add(observer);
    }

    // ── UC2 ──────────────────────────────────────────────────────────────────

    /**
     * UC2: Request a booking for an available time slot.
     *
     * @throws EntityNotFoundException       if client, consultant, service, or slot is unknown
     * @throws ConsultantNotApprovedException if the consultant is not yet approved
     * @throws SlotUnavailableException       if the time slot is already booked
     */
    public Booking requestBooking(String clientId, String consultantId,
                                  String serviceId, String slotId) {
        clientRepository.findById(clientId)
                .orElseThrow(() -> new EntityNotFoundException("Client not found: " + clientId));
        Consultant consultant = consultantRepository.findById(consultantId)
                .orElseThrow(() -> new EntityNotFoundException("Consultant not found: " + consultantId));
        serviceRepository.findById(serviceId)
                .orElseThrow(() -> new EntityNotFoundException("Service not found: " + serviceId));
        TimeSlot slot = timeSlotRepository.findById(slotId)
                .orElseThrow(() -> new EntityNotFoundException("TimeSlot not found: " + slotId));

        if (!consultant.isApproved()) {
            throw new ConsultantNotApprovedException(consultantId);
        }
        if (!slot.isAvailable()) {
            throw new SlotUnavailableException(slotId);
        }

        slot.book();
        timeSlotRepository.save(slot);

        Booking booking = new Booking(
                UUID.randomUUID().toString(),
                clientId, consultantId, serviceId, slotId,
                timeProvider.now()
        );
        bookingRepository.save(booking);
        notifyObservers(booking, "BOOKING_REQUESTED",
                "Your booking request (ID: " + booking.getId() + ") has been submitted.");
        return booking;
    }

    // ── UC3 ──────────────────────────────────────────────────────────────────

    /**
     * UC3: Cancel a booking and apply the configured cancellation policy.
     *
     * @throws BookingNotFoundException     if the booking is not found
     * @throws UnauthorizedActionException  if the requester is not the booking's client
     */
    public double cancelBooking(String bookingId, String clientId) {
        Booking booking = getBookingOrThrow(bookingId);
        if (!booking.getClientId().equals(clientId)) {
            throw new UnauthorizedActionException(
                    "Client " + clientId + " is not authorized to cancel booking " + bookingId);
        }

        double refundAmount = 0.0;
        if (booking.getStatus() == BookingStatus.PAID) {
            Payment payment = paymentRepository.findByBookingId(bookingId).orElse(null);
            double paid = (payment != null) ? payment.getAmount() : 0.0;
            refundAmount = systemPolicy.getCancellationPolicy().apply(booking, paid);
        }

        booking.cancel();
        bookingRepository.save(booking);

        // Release the time slot
        timeSlotRepository.findById(booking.getTimeSlotId())
                .ifPresent(slot -> {
                    slot.release();
                    timeSlotRepository.save(slot);
                });

        notifyObservers(booking, "BOOKING_CANCELLED",
                "Your booking (ID: " + bookingId + ") has been cancelled. Refund: $" +
                        String.format("%.2f", refundAmount));
        return refundAmount;
    }

    // ── UC4 ──────────────────────────────────────────────────────────────────

    /**
     * UC4: Get all bookings for a client.
     *
     * @param clientId the client's ID
     * @return list of bookings (may be empty)
     */
    public List<Booking> getBookingsForClient(String clientId) {
        return bookingRepository.findByClientId(clientId);
    }

    /**
     * UC4: Get all bookings for a consultant.
     *
     * @param consultantId the consultant's ID
     * @return list of bookings (may be empty)
     */
    public List<Booking> getBookingsForConsultant(String consultantId) {
        return bookingRepository.findByConsultantId(consultantId);
    }

    // ── UC9 ──────────────────────────────────────────────────────────────────

    /**
     * UC9: Consultant accepts a booking request → moves to PENDING_PAYMENT.
     *
     * @throws BookingNotFoundException    if the booking is not found
     * @throws UnauthorizedActionException if the actor is not the booking's consultant
     */
    public void acceptBooking(String bookingId, String consultantId) {
        Booking booking = getBookingOrThrow(bookingId);
        assertConsultant(booking, consultantId);
        booking.accept();
        bookingRepository.save(booking);
        notifyObservers(booking, "BOOKING_ACCEPTED",
                "Your booking (ID: " + bookingId + ") has been accepted. Please proceed with payment.");
    }

    /**
     * UC9: Consultant rejects a booking request → moves to REJECTED.
     *
     * @throws BookingNotFoundException    if the booking is not found
     * @throws UnauthorizedActionException if the actor is not the booking's consultant
     */
    public void rejectBooking(String bookingId, String consultantId) {
        Booking booking = getBookingOrThrow(bookingId);
        assertConsultant(booking, consultantId);
        booking.reject();
        bookingRepository.save(booking);

        // Release the time slot on rejection
        timeSlotRepository.findById(booking.getTimeSlotId())
                .ifPresent(slot -> {
                    slot.release();
                    timeSlotRepository.save(slot);
                });

        notifyObservers(booking, "BOOKING_REJECTED",
                "Your booking (ID: " + bookingId + ") has been rejected by the consultant.");
    }

    // ── UC10 ─────────────────────────────────────────────────────────────────

    /**
     * UC10: Consultant marks a booking as completed (only valid if booking is in PAID state).
     *
     * @throws BookingNotFoundException      if the booking is not found
     * @throws UnauthorizedActionException   if the actor is not the booking's consultant
     * @throws InvalidBookingStateException  if the booking has not been paid
     */
    public void completeBooking(String bookingId, String consultantId) {
        Booking booking = getBookingOrThrow(bookingId);
        assertConsultant(booking, consultantId);
        booking.complete();
        bookingRepository.save(booking);
        notifyObservers(booking, "BOOKING_COMPLETED",
                "Your booking (ID: " + bookingId + ") has been completed. Thank you!");
    }

    // ── Internal helpers ─────────────────────────────────────────────────────

    /** Marks booking as PAID (called by PaymentService after successful payment). */
    public void markAsPaid(String bookingId) {
        Booking booking = getBookingOrThrow(bookingId);
        booking.pay();
        bookingRepository.save(booking);
    }

    public Booking getBookingOrThrow(String bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException(bookingId));
    }

    private void assertConsultant(Booking booking, String consultantId) {
        if (!booking.getConsultantId().equals(consultantId)) {
            throw new UnauthorizedActionException(
                    "Consultant " + consultantId + " is not authorized for booking " + booking.getId());
        }
    }

    private void notifyObservers(Booking booking, String eventType, String message) {
        if (systemPolicy.isNotificationsEnabled()) {
            BookingEvent event = new BookingEvent(booking, eventType, message);
            observers.forEach(obs -> obs.onBookingEvent(event));
        }
    }
}
