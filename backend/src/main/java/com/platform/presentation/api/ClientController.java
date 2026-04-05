package com.platform.presentation.api;

import com.platform.application.ClientService;
import com.platform.domain.Client;

import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/clients")
public class ClientController {

    private final ClientService clientService;

    public ClientController(ClientService clientService) {
        this.clientService = clientService;
    }

    @PostMapping
    public Client registerClient(@RequestBody Map<String, String> body) {
        return clientService.registerClient(body.get("name"), body.get("email"));
    }

    @GetMapping
    public List<Client> getAllClients() {
        return clientService.getAllClients();
    }

    @GetMapping("/{id}")
    public Client getClient(@PathVariable String id) {
        return clientService.getClient(id);
    }
}
