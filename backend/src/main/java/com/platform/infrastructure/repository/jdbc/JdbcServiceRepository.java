package com.platform.infrastructure.repository.jdbc;

import com.platform.domain.ConsultingService;
import com.platform.infrastructure.repository.ServiceRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.util.List;
import java.util.Optional;

public class JdbcServiceRepository implements ServiceRepository {

    private final JdbcTemplate jdbc;

    private final RowMapper<ConsultingService> mapper = (rs, i) ->
            new ConsultingService(rs.getString("id"), rs.getString("name"), rs.getString("type"),
                    rs.getInt("duration_minutes"), rs.getDouble("base_price"));

    public JdbcServiceRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void save(ConsultingService service) {
        jdbc.update(
            "INSERT INTO services (id, name, type, duration_minutes, base_price) VALUES (?, ?, ?, ?, ?) ON CONFLICT (id) DO UPDATE SET name=?, type=?, duration_minutes=?, base_price=?",
            service.getId(), service.getName(), service.getType(), service.getDurationMinutes(), service.getBasePrice(),
            service.getName(), service.getType(), service.getDurationMinutes(), service.getBasePrice());
    }

    @Override
    public Optional<ConsultingService> findById(String id) {
        var list = jdbc.query("SELECT * FROM services WHERE id=?", mapper, id);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    @Override
    public List<ConsultingService> findAll() {
        return jdbc.query("SELECT * FROM services", mapper);
    }
}
