package com.platform.infrastructure.repository.jdbc;

import com.platform.domain.Client;
import com.platform.infrastructure.repository.ClientRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.util.List;
import java.util.Optional;

public class JdbcClientRepository implements ClientRepository {

    private final JdbcTemplate jdbc;

    private final RowMapper<Client> mapper = (rs, i) ->
            new Client(rs.getString("id"), rs.getString("name"), rs.getString("email"));

    public JdbcClientRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void save(Client client) {
        jdbc.update(
            "INSERT INTO clients (id, name, email) VALUES (?, ?, ?) ON CONFLICT (id) DO UPDATE SET name=?, email=?",
            client.getId(), client.getName(), client.getEmail(), client.getName(), client.getEmail());
    }

    @Override
    public Optional<Client> findById(String id) {
        var list = jdbc.query("SELECT * FROM clients WHERE id=?", mapper, id);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    @Override
    public List<Client> findAll() {
        return jdbc.query("SELECT * FROM clients", mapper);
    }
}
