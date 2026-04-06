package com.platform.domain;

import java.time.LocalDateTime;
import java.util.Objects;

/** An in-memory payment record tied to a booking. */
public class Payment {

    private final String id;
    private final String bookingId;
    private final String clientId;
    private final double amount;
    private final PaymentType paymentType;
    private final String transactionId;
    private final LocalDateTime paidAt;
    private String status; // e.g., "SUCCESS", "REFUNDED"

    public Payment(String id, String bookingId, String clientId,
                   double amount, PaymentType paymentType,
                   String transactionId, LocalDateTime paidAt) {
        this.id = Objects.requireNonNull(id, "id");
        this.bookingId = Objects.requireNonNull(bookingId, "bookingId");
        this.clientId = Objects.requireNonNull(clientId, "clientId");
        this.amount = amount;
        this.paymentType = Objects.requireNonNull(paymentType, "paymentType");
        this.transactionId = Objects.requireNonNull(transactionId, "transactionId");
        this.paidAt = Objects.requireNonNull(paidAt, "paidAt");
        this.status = "SUCCESS";
    }

    public String        getId()            { return id; }
    public String        getBookingId()     { return bookingId; }
    public String        getClientId()      { return clientId; }
    public double        getAmount()        { return amount; }
    public PaymentType   getPaymentType()   { return paymentType; }
    public String        getTransactionId() { return transactionId; }
    public LocalDateTime getPaidAt()        { return paidAt; }
    public String        getStatus()        { return status; }

    public void setStatus(String status)    { this.status = status; }

    @Override
    public String toString() {
        return String.format(
                "Payment{id='%s', booking='%s', txn='%s', amount=%.2f, type=%s, status=%s, paidAt=%s}",
                id, bookingId, transactionId, amount, paymentType, status, paidAt);
    }
}
