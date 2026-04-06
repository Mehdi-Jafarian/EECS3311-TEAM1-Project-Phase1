package com.platform.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.platform.domain.state.BookingStateHandler;
import com.platform.domain.state.RequestedState;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Core domain entity representing a booking between a Client and a Consultant.
 *
 * <p><b>State Pattern (Context):</b> delegates all lifecycle transitions to
 * the current {@link BookingStateHandler}. Concrete state classes enforce
 * which transitions are valid and set the next state.
 */
public class Booking {

    private final String id;
    private final String clientId;
    private final String consultantId;
    private final String serviceId;
    private final String timeSlotId;
    private final LocalDateTime createdAt;

    /** Current state handler — replaced on every valid transition. */
    @JsonIgnore
    private BookingStateHandler stateHandler;

    public Booking(String id, String clientId, String consultantId,
                   String serviceId, String timeSlotId, LocalDateTime createdAt) {
        this.id = Objects.requireNonNull(id, "id");
        this.clientId = Objects.requireNonNull(clientId, "clientId");
        this.consultantId = Objects.requireNonNull(consultantId, "consultantId");
        this.serviceId = Objects.requireNonNull(serviceId, "serviceId");
        this.timeSlotId = Objects.requireNonNull(timeSlotId, "timeSlotId");
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt");
        this.stateHandler = new RequestedState();
    }

    // ── Delegated lifecycle transitions ─────────────────────────────────────

    /** Confirms the booking (optional intermediate step). */
    public void confirm()  { stateHandler.confirm(this); }

    /** Consultant accepts — moves to PENDING_PAYMENT. */
    public void accept()   { stateHandler.accept(this); }

    /** Consultant rejects — moves to REJECTED. */
    public void reject()   { stateHandler.reject(this); }

    /** Client pays — moves to PAID. */
    public void pay()      { stateHandler.pay(this); }

    /** Consultant completes — moves to COMPLETED. */
    public void complete() { stateHandler.complete(this); }

    /** Client/system cancels — moves to CANCELLED. */
    public void cancel()   { stateHandler.cancel(this); }

    // ── Accessors ────────────────────────────────────────────────────────────

    public String        getId()          { return id; }
    public String        getClientId()    { return clientId; }
    public String        getConsultantId(){ return consultantId; }
    public String        getServiceId()   { return serviceId; }
    public String        getTimeSlotId()  { return timeSlotId; }
    public LocalDateTime getCreatedAt()   { return createdAt; }
    public BookingStatus getStatus()      { return stateHandler.getStatus(); }

    /**
     * Called by concrete state handlers to switch to the next state.
     * Package-friendly via same domain package.
     */
    public void setStateHandler(BookingStateHandler handler) {
        this.stateHandler = Objects.requireNonNull(handler, "handler");
    }

    @Override
    public String toString() {
        return String.format(
                "Booking{id='%s', client='%s', consultant='%s', service='%s', slot='%s', status=%s, created=%s}",
                id, clientId, consultantId, serviceId, timeSlotId, getStatus(), createdAt);
    }
}
