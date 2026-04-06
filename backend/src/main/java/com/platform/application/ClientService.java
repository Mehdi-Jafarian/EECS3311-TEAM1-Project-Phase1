package com.platform.application;

import com.platform.domain.Client;
import com.platform.domain.exception.EntityNotFoundException;
import com.platform.infrastructure.repository.ClientRepository;

import java.util.List;
import java.util.UUID;

/**
 * Application service for client registration and lookup.
 */
public class ClientService {

    private final ClientRepository clientRepository;

    public ClientService(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    /**
     * Registers a new client.
     *
     * @param name  the client's display name
     * @param email the client's email
     * @return the created {@link Client}
     */
    public Client registerClient(String name, String email) {
        Client client = new Client(UUID.randomUUID().toString(), name, email);
        clientRepository.save(client);
        return client;
    }

    /**
     * Retrieves a client by ID.
     *
     * @param id the client's ID
     * @return the {@link Client}
     * @throws EntityNotFoundException if not found
     */
    public Client getClient(String id) {
        return clientRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Client not found: " + id));
    }

    /**
     * Returns all registered clients.
     */
    public List<Client> getAllClients() {
        return clientRepository.findAll();
    }
}
