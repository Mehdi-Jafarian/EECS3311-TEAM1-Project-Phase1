package com.platform.infrastructure.repository.jdbc;

import com.platform.domain.Notification;
import com.platform.infrastructure.repository.NotificationRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.util.List;

public class JdbcNotificationRepository implements NotificationRepository {

    private final JdbcTemplate jdbc;

    private final RowMapper<Notification> mapper = (rs, i) ->
            new Notification(rs.getString("id"), rs.getString("recipient_id"),
                    rs.getString("message"), rs.getTimestamp("timestamp").toLocalDateTime());

    public JdbcNotificationRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void save(Notification notification) {
        jdbc.update("INSERT INTO notifications (id, recipient_id, message, timestamp) VALUES (?, ?, ?, ?)",
                notification.getId(), notification.getRecipientId(), notification.getMessage(), notification.getTimestamp());
    }

    @Override
    public List<Notification> findByRecipientId(String recipientId) {
        return jdbc.query("SELECT * FROM notifications WHERE recipient_id=?", mapper, recipientId);
    }

    @Override
    public List<Notification> findAll() {
        return jdbc.query("SELECT * FROM notifications", mapper);
    }
}
