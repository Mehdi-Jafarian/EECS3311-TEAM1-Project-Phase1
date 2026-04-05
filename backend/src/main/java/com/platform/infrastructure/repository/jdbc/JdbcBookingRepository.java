package com.platform.infrastructure.repository.jdbc;

import com.platform.domain.Booking;
import com.platform.domain.BookingStatus;
import com.platform.domain.state.*;
import com.platform.infrastructure.repository.BookingRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.util.List;
import java.util.Optional;

public class JdbcBookingRepository implements BookingRepository {

    private final JdbcTemplate jdbc;

    private final RowMapper<Booking> mapper = (rs, i) -> {
        Booking b = new Booking(
                rs.getString("id"), rs.getString("client_id"),
                rs.getString("consultant_id"), rs.getString("service_id"),
                rs.getString("time_slot_id"), rs.getTimestamp("created_at").toLocalDateTime());
        BookingStatus status = BookingStatus.valueOf(rs.getString("status"));
        b.setStateHandler(handlerFor(status));
        return b;
    };

    public JdbcBookingRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void save(Booking booking) {
        jdbc.update(
            "INSERT INTO bookings (id, client_id, consultant_id, service_id, time_slot_id, status, created_at) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?) ON CONFLICT (id) DO UPDATE SET status=?",
            booking.getId(), booking.getClientId(), booking.getConsultantId(),
            booking.getServiceId(), booking.getTimeSlotId(), booking.getStatus().name(),
            booking.getCreatedAt(), booking.getStatus().name());
    }

    @Override
    public Optional<Booking> findById(String id) {
        var list = jdbc.query("SELECT * FROM bookings WHERE id=?", mapper, id);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    @Override
    public List<Booking> findByClientId(String clientId) {
        return jdbc.query("SELECT * FROM bookings WHERE client_id=?", mapper, clientId);
    }

    @Override
    public List<Booking> findByConsultantId(String consultantId) {
        return jdbc.query("SELECT * FROM bookings WHERE consultant_id=?", mapper, consultantId);
    }

    @Override
    public List<Booking> findAll() {
        return jdbc.query("SELECT * FROM bookings", mapper);
    }

    private static BookingStateHandler handlerFor(BookingStatus status) {
        return switch (status) {
            case REQUESTED -> new RequestedState();
            case CONFIRMED -> new PendingPaymentState();
            case PENDING_PAYMENT -> new PendingPaymentState();
            case PAID -> new PaidState();
            case COMPLETED -> new CompletedState();
            case CANCELLED -> new CancelledState();
            case REJECTED -> new RejectedState();
        };
    }
}
