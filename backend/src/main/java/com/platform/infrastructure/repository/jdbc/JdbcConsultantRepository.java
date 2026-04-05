package com.platform.infrastructure.repository.jdbc;

import com.platform.domain.Consultant;
import com.platform.domain.ConsultantStatus;
import com.platform.infrastructure.repository.ConsultantRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.util.List;
import java.util.Optional;

public class JdbcConsultantRepository implements ConsultantRepository {

    private final JdbcTemplate jdbc;

    private final RowMapper<Consultant> mapper = (rs, i) -> {
        Consultant c = new Consultant(rs.getString("id"), rs.getString("name"), rs.getString("email"));
        String status = rs.getString("status");
        if ("APPROVED".equals(status)) c.approve();
        else if ("REJECTED".equals(status)) c.reject();
        return c;
    };

    public JdbcConsultantRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void save(Consultant consultant) {
        jdbc.update(
            "INSERT INTO consultants (id, name, email, status) VALUES (?, ?, ?, ?) ON CONFLICT (id) DO UPDATE SET name=?, email=?, status=?",
            consultant.getId(), consultant.getName(), consultant.getEmail(), consultant.getStatus().name(),
            consultant.getName(), consultant.getEmail(), consultant.getStatus().name());
    }

    @Override
    public Optional<Consultant> findById(String id) {
        var list = jdbc.query("SELECT * FROM consultants WHERE id=?", mapper, id);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    @Override
    public List<Consultant> findAll() {
        return jdbc.query("SELECT * FROM consultants", mapper);
    }
}
