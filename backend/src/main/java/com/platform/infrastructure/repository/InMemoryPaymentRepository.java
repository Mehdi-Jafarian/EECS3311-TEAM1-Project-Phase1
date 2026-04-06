package com.platform.infrastructure.repository;

import com.platform.domain.Payment;

import java.util.*;
import java.util.stream.Collectors;

public class InMemoryPaymentRepository implements PaymentRepository {

    private final Map<String, Payment> store = new HashMap<>();

    @Override
    public void save(Payment payment) {
        store.put(payment.getId(), payment);
    }

    @Override
    public Optional<Payment> findById(String id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<Payment> findByClientId(String clientId) {
        return store.values().stream()
                .filter(p -> p.getClientId().equals(clientId))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Payment> findByBookingId(String bookingId) {
        return store.values().stream()
                .filter(p -> p.getBookingId().equals(bookingId))
                .findFirst();
    }

    @Override
    public List<Payment> findAll() {
        return new ArrayList<>(store.values());
    }
}
