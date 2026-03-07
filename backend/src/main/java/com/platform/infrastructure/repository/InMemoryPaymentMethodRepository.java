package com.platform.infrastructure.repository;

import com.platform.domain.PaymentMethod;

import java.util.*;
import java.util.stream.Collectors;

public class InMemoryPaymentMethodRepository implements PaymentMethodRepository {

    private final Map<String, PaymentMethod> store = new HashMap<>();

    @Override
    public void save(PaymentMethod paymentMethod) {
        store.put(paymentMethod.getId(), paymentMethod);
    }

    @Override
    public Optional<PaymentMethod> findById(String id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<PaymentMethod> findByClientId(String clientId) {
        return store.values().stream()
                .filter(pm -> pm.getClientId().equals(clientId))
                .collect(Collectors.toList());
    }

    @Override
    public void delete(String id) {
        store.remove(id);
    }
}
