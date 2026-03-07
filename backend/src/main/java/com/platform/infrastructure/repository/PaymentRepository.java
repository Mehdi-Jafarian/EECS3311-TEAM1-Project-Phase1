package com.platform.infrastructure.repository;

import com.platform.domain.Payment;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository {
    void save(Payment payment);
    Optional<Payment> findById(String id);
    List<Payment> findByClientId(String clientId);
    Optional<Payment> findByBookingId(String bookingId);
    List<Payment> findAll();
}
