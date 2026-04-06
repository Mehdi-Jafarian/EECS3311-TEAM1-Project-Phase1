package com.platform.infrastructure.repository;

import com.platform.domain.TimeSlot;

import java.util.*;
import java.util.stream.Collectors;

public class InMemoryTimeSlotRepository implements TimeSlotRepository {

    private final Map<String, TimeSlot> store = new HashMap<>();

    @Override
    public void save(TimeSlot timeSlot) {
        store.put(timeSlot.getId(), timeSlot);
    }

    @Override
    public Optional<TimeSlot> findById(String id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<TimeSlot> findByConsultantId(String consultantId) {
        return store.values().stream()
                .filter(ts -> ts.getConsultantId().equals(consultantId))
                .collect(Collectors.toList());
    }

    @Override
    public List<TimeSlot> findAll() {
        return new ArrayList<>(store.values());
    }
}
