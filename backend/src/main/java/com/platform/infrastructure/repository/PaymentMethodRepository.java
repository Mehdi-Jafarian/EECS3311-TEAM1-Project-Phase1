package com.platform.infrastructure.repository;

import com.platform.domain.PaymentMethod;

import java.util.List;
import java.util.Optional;

public interface PaymentMethodRepository {
    void save(PaymentMethod paymentMethod);
    Optional<PaymentMethod> findById(String id);
    List<PaymentMethod> findByClientId(String clientId);
    void delete(String id);
}
