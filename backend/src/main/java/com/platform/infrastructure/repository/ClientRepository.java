package com.platform.infrastructure.repository;

import com.platform.domain.Client;

import java.util.List;
import java.util.Optional;

public interface ClientRepository {
    void save(Client client);
    Optional<Client> findById(String id);
    List<Client> findAll();
}
