package com.platform.infrastructure.repository.jdbc;

import com.platform.domain.TimeSlot;
import com.platform.infrastructure.repository.TimeSlotRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.util.List;
import java.util.Optional;

public class JdbcTimeSlotRepository implements TimeSlotRepository {

    private final JdbcTemplate jdbc;

    private final RowMapper<TimeSlot> mapper = (rs, i) -> {
        TimeSlot slot = new TimeSlot(
                rs.getString("id"), rs.getString("consultant_id"),
                rs.getDate("date").toLocalDate(),
                rs.getTime("start_time").toLocalTime(),
                rs.getTime("end_time").toLocalTime());
        if (!rs.getBoolean("available")) slot.book();
        return slot;
    };

    public JdbcTimeSlotRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void save(TimeSlot slot) {
        jdbc.update(
            "INSERT INTO time_slots (id, consultant_id, date, start_time, end_time, available) VALUES (?, ?, ?, ?, ?, ?) " +
            "ON CONFLICT (id) DO UPDATE SET consultant_id=?, date=?, start_time=?, end_time=?, available=?",
            slot.getId(), slot.getConsultantId(), slot.getDate(), slot.getStartTime(), slot.getEndTime(), slot.isAvailable(),
            slot.getConsultantId(), slot.getDate(), slot.getStartTime(), slot.getEndTime(), slot.isAvailable());
    }

    @Override
    public Optional<TimeSlot> findById(String id) {
        var list = jdbc.query("SELECT * FROM time_slots WHERE id=?", mapper, id);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    @Override
    public List<TimeSlot> findByConsultantId(String consultantId) {
        return jdbc.query("SELECT * FROM time_slots WHERE consultant_id=?", mapper, consultantId);
    }

    @Override
    public List<TimeSlot> findAll() {
        return jdbc.query("SELECT * FROM time_slots", mapper);
    }
}
