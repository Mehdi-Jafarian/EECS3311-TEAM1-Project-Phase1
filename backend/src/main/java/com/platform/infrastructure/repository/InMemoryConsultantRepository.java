package com.platform.infrastructure.repository;

import com.platform.domain.Consultant;

import java.util.*;

public class InMemoryConsultantRepository implements ConsultantRepository {

    private final Map<String, Consultant> store = new HashMap<>();

    @Override
    public void save(Consultant consultant) {
        store.put(consultant.getId(), consultant);
    }

    @Override
    public Optional<Consultant> findById(String id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<Consultant> findAll() {
        return new ArrayList<>(store.values());
    }
}
