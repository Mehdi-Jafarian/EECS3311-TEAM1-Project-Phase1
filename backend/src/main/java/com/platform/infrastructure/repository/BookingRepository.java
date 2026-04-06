package com.platform.infrastructure.repository;

import com.platform.domain.Booking;

import java.util.List;
import java.util.Optional;

public interface BookingRepository {
    void save(Booking booking);
    Optional<Booking> findById(String id);
    List<Booking> findByClientId(String clientId);
    List<Booking> findByConsultantId(String consultantId);
    List<Booking> findAll();
}
