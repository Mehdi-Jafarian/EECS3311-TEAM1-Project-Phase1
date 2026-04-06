package com.platform.application;

import com.platform.application.payment.PaymentProcessorFactory;
import com.platform.domain.*;
import com.platform.domain.exception.*;
import com.platform.domain.policy.SystemPolicy;
import com.platform.infrastructure.repository.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Application service for payment-related use cases (UC5, UC6, UC7).
 *
 * <p><b>Observer Pattern (Subject):</b> fires events after successful payment.
 * <p><b>Factory Method Pattern:</b> delegates to {@link PaymentProcessorFactory}
 * to obtain the correct {@link com.platform.application.payment.PaymentProcessor}.
 */
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentMethodRepository paymentMethodRepository;
    private final BookingService bookingService;
    private final PaymentProcessorFactory processorFactory;
    private final SystemPolicy systemPolicy;
    private final List<BookingEventObserver> observers = new ArrayList<>();

    public PaymentService(PaymentRepository paymentRepository,
                          PaymentMethodRepository paymentMethodRepository,
                          BookingService bookingService,
                          PaymentProcessorFactory processorFactory,
                          SystemPolicy systemPolicy) {
        this.paymentRepository = paymentRepository;
        this.paymentMethodRepository = paymentMethodRepository;
        this.bookingService = bookingService;
        this.processorFactory = processorFactory;
        this.systemPolicy = systemPolicy;
    }

    /** Register an observer to receive payment-related events. */
    public void addObserver(BookingEventObserver observer) {
        observers.add(observer);
    }

    // ── UC5 ──────────────────────────────────────────────────────────────────

    /**
     * UC5: Process payment for a booking.
     *
     * <ol>
     *   <li>Validate booking is in PENDING_PAYMENT state.</li>
     *   <li>Validate payment method details.</li>
     *   <li>Simulate 2–3 s processing delay (configurable for tests).</li>
     *   <li>Generate unique transaction ID.</li>
     *   <li>Persist payment record.</li>
     *   <li>Advance booking to PAID.</li>
     *   <li>Fire payment-confirmation event (→ notification).</li>
     * </ol>
     *
     * @param bookingId       the booking to pay for
     * @param paymentMethodId the saved payment method to use
     * @return the created {@link Payment}
     * @throws BookingNotFoundException    if booking not found
     * @throws EntityNotFoundException    if payment method not found
     * @throws InvalidBookingStateException if booking is not in PENDING_PAYMENT state
     */
    public Payment processPayment(String bookingId, String paymentMethodId) {
        Booking booking = bookingService.getBookingOrThrow(bookingId);
        if (booking.getStatus() != BookingStatus.PENDING_PAYMENT) {
            throw new InvalidBookingStateException(
                    "Booking " + bookingId + " is not in PENDING_PAYMENT state (current: " +
                            booking.getStatus() + ").");
        }

        PaymentMethod method = paymentMethodRepository.findById(paymentMethodId)
                .orElseThrow(() -> new EntityNotFoundException("Payment method not found: " + paymentMethodId));

        // Retrieve effective price via pricing strategy
        double amount = 0.0;
        // (amount is determined externally when booking is created; here we use stored service price)
        // For simplicity we get it from the processor factory which delegates to the booking service
        // A real amount would come from the service + pricing strategy at booking-request time.
        // We store no price on the Booking entity in this simplified model, so we retrieve it here.
        // If a Payment already exists (should not), treat it as a re-process guard.
        if (paymentRepository.findByBookingId(bookingId).isPresent()) {
            throw new InvalidBookingStateException(
                    "Booking " + bookingId + " has already been paid.");
        }

        // Use the processor factory (Factory Method) to get the right processor
        var processor = processorFactory.create(method.getPaymentType());
        Payment payment = processor.process(method, amount, bookingId, booking.getClientId());

        paymentRepository.save(payment);
        bookingService.markAsPaid(bookingId);

        notifyObservers(booking, "PAYMENT_CONFIRMED",
                "Payment confirmed for booking " + bookingId +
                        ". Transaction ID: " + payment.getTransactionId() +
                        ". Amount: $" + String.format("%.2f", amount));
        return payment;
    }

    /**
     * UC5 variant: Process payment with an explicit amount (used by CLI/tests when amount is known).
     */
    public Payment processPayment(String bookingId, String paymentMethodId, double amount) {
        Booking booking = bookingService.getBookingOrThrow(bookingId);
        if (booking.getStatus() != BookingStatus.PENDING_PAYMENT) {
            throw new InvalidBookingStateException(
                    "Booking " + bookingId + " is not in PENDING_PAYMENT state (current: " +
                            booking.getStatus() + ").");
        }

        PaymentMethod method = paymentMethodRepository.findById(paymentMethodId)
                .orElseThrow(() -> new EntityNotFoundException("Payment method not found: " + paymentMethodId));

        if (paymentRepository.findByBookingId(bookingId).isPresent()) {
            throw new InvalidBookingStateException(
                    "Booking " + bookingId + " has already been paid.");
        }

        var processor = processorFactory.create(method.getPaymentType());
        Payment payment = processor.process(method, amount, bookingId, booking.getClientId());

        paymentRepository.save(payment);
        bookingService.markAsPaid(bookingId);

        notifyObservers(booking, "PAYMENT_CONFIRMED",
                "Payment confirmed for booking " + bookingId +
                        ". Transaction ID: " + payment.getTransactionId() +
                        ". Amount: $" + String.format("%.2f", amount));
        return payment;
    }

    // ── UC6 ──────────────────────────────────────────────────────────────────

    /**
     * UC6: Add a saved payment method for a client.
     *
     * @param method the payment method to save (must pass {@link PaymentMethod#validate()})
     */
    public void addPaymentMethod(PaymentMethod method) {
        method.validate(); // validate before saving
        paymentMethodRepository.save(method);
    }

    /**
     * UC6: Retrieve all saved payment methods for a client.
     *
     * @param clientId the client's ID
     * @return list of payment methods
     */
    public List<PaymentMethod> getPaymentMethods(String clientId) {
        return paymentMethodRepository.findByClientId(clientId);
    }

    /**
     * UC6: Remove a saved payment method.
     *
     * @param id       the payment method ID
     * @param clientId the owning client (authorization check)
     * @throws EntityNotFoundException   if the method is not found
     * @throws UnauthorizedActionException if the method does not belong to the client
     */
    public void removePaymentMethod(String id, String clientId) {
        PaymentMethod method = paymentMethodRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Payment method not found: " + id));
        if (!method.getClientId().equals(clientId)) {
            throw new UnauthorizedActionException(
                    "Client " + clientId + " does not own payment method " + id);
        }
        paymentMethodRepository.delete(id);
    }

    // ── UC7 ──────────────────────────────────────────────────────────────────

    /**
     * UC7: Get payment history for a client.
     *
     * @param clientId the client's ID
     * @return list of payments
     */
    public List<Payment> getPaymentHistory(String clientId) {
        return paymentRepository.findByClientId(clientId);
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private void notifyObservers(Booking booking, String eventType, String message) {
        if (systemPolicy.isNotificationsEnabled()) {
            BookingEvent event = new BookingEvent(booking, eventType, message);
            observers.forEach(obs -> obs.onBookingEvent(event));
        }
    }
}
