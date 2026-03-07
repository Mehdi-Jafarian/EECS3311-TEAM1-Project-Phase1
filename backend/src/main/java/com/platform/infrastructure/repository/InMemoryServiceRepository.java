package com.platform.infrastructure.repository;

import com.platform.domain.ConsultingService;

import java.util.*;

public class InMemoryServiceRepository implements ServiceRepository {

    private final Map<String, ConsultingService> store = new HashMap<>();

    @Override
    public void save(ConsultingService service) {
        store.put(service.getId(), service);
    }

    @Override
    public Optional<ConsultingService> findById(String id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<ConsultingService> findAll() {
        return new ArrayList<>(store.values());
    }
}
