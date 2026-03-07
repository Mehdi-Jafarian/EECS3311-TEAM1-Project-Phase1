package com.platform.infrastructure.repository;

import com.platform.domain.Client;

import java.util.*;

public class InMemoryClientRepository implements ClientRepository {

    private final Map<String, Client> store = new HashMap<>();

    @Override
    public void save(Client client) {
        store.put(client.getId(), client);
    }

    @Override
    public Optional<Client> findById(String id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<Client> findAll() {
        return new ArrayList<>(store.values());
    }
}
