package com.platform.infrastructure.repository;

import com.platform.domain.TimeSlot;

import java.util.List;
import java.util.Optional;

public interface TimeSlotRepository {
    void save(TimeSlot timeSlot);
    Optional<TimeSlot> findById(String id);
    List<TimeSlot> findByConsultantId(String consultantId);
    List<TimeSlot> findAll();
}
