package com.platform.infrastructure.repository.jdbc;

import com.platform.domain.Payment;
import com.platform.domain.PaymentType;
import com.platform.infrastructure.repository.PaymentRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.util.List;
import java.util.Optional;

public class JdbcPaymentRepository implements PaymentRepository {

    private final JdbcTemplate jdbc;

    private final RowMapper<Payment> mapper = (rs, i) -> {
        Payment p = new Payment(
                rs.getString("id"), rs.getString("booking_id"), rs.getString("client_id"),
                rs.getDouble("amount"), PaymentType.valueOf(rs.getString("payment_type")),
                rs.getString("transaction_id"), rs.getTimestamp("paid_at").toLocalDateTime());
        p.setStatus(rs.getString("status"));
        return p;
    };

    public JdbcPaymentRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void save(Payment payment) {
        jdbc.update(
            "INSERT INTO payments (id, booking_id, client_id, amount, payment_type, transaction_id, paid_at, status) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?) ON CONFLICT (id) DO UPDATE SET status=?",
            payment.getId(), payment.getBookingId(), payment.getClientId(), payment.getAmount(),
            payment.getPaymentType().name(), payment.getTransactionId(), payment.getPaidAt(),
            payment.getStatus(), payment.getStatus());
    }

    @Override
    public Optional<Payment> findById(String id) {
        var list = jdbc.query("SELECT * FROM payments WHERE id=?", mapper, id);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    @Override
    public List<Payment> findByClientId(String clientId) {
        return jdbc.query("SELECT * FROM payments WHERE client_id=?", mapper, clientId);
    }

    @Override
    public Optional<Payment> findByBookingId(String bookingId) {
        var list = jdbc.query("SELECT * FROM payments WHERE booking_id=?", mapper, bookingId);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    @Override
    public List<Payment> findAll() {
        return jdbc.query("SELECT * FROM payments", mapper);
    }
}
