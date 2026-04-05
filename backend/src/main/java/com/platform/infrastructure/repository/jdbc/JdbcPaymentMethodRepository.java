package com.platform.infrastructure.repository.jdbc;

import com.platform.domain.*;
import com.platform.infrastructure.repository.PaymentMethodRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.util.List;
import java.util.Optional;

public class JdbcPaymentMethodRepository implements PaymentMethodRepository {

    private final JdbcTemplate jdbc;

    private final RowMapper<PaymentMethod> mapper = (rs, i) -> {
        String id = rs.getString("id");
        String clientId = rs.getString("client_id");
        PaymentType type = PaymentType.valueOf(rs.getString("payment_type"));
        return switch (type) {
            case CREDIT_CARD -> new CreditCardPaymentMethod(id, clientId,
                    rs.getString("card_number"), rs.getInt("expiry_month"), rs.getInt("expiry_year"), rs.getString("cvv"));
            case DEBIT_CARD -> new DebitCardPaymentMethod(id, clientId,
                    rs.getString("card_number"), rs.getInt("expiry_month"), rs.getInt("expiry_year"), rs.getString("cvv"));
            case PAYPAL -> new PayPalPaymentMethod(id, clientId, rs.getString("email"));
            case BANK_TRANSFER -> new BankTransferPaymentMethod(id, clientId,
                    rs.getString("account_number"), rs.getString("routing_number"));
        };
    };

    public JdbcPaymentMethodRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void save(PaymentMethod pm) {
        String cardNumber = null, cvv = null, email = null, accountNumber = null, routingNumber = null;
        Integer expiryMonth = null, expiryYear = null;

        if (pm instanceof CreditCardPaymentMethod cc) {
            cardNumber = cc.getCardNumber(); expiryMonth = cc.getExpiryMonth(); expiryYear = cc.getExpiryYear(); cvv = cc.getCvv();
        } else if (pm instanceof DebitCardPaymentMethod dc) {
            cardNumber = dc.getCardNumber(); expiryMonth = dc.getExpiryMonth(); expiryYear = dc.getExpiryYear(); cvv = dc.getCvv();
        } else if (pm instanceof PayPalPaymentMethod pp) {
            email = pp.getEmail();
        } else if (pm instanceof BankTransferPaymentMethod bt) {
            accountNumber = bt.getAccountNumber(); routingNumber = bt.getRoutingNumber();
        }

        jdbc.update(
            "INSERT INTO payment_methods (id, client_id, payment_type, card_number, expiry_month, expiry_year, cvv, email, account_number, routing_number) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ON CONFLICT (id) DO NOTHING",
            pm.getId(), pm.getClientId(), pm.getPaymentType().name(),
            cardNumber, expiryMonth, expiryYear, cvv, email, accountNumber, routingNumber);
    }

    @Override
    public Optional<PaymentMethod> findById(String id) {
        var list = jdbc.query("SELECT * FROM payment_methods WHERE id=?", mapper, id);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    @Override
    public List<PaymentMethod> findByClientId(String clientId) {
        return jdbc.query("SELECT * FROM payment_methods WHERE client_id=?", mapper, clientId);
    }

    @Override
    public void delete(String id) {
        jdbc.update("DELETE FROM payment_methods WHERE id=?", id);
    }
}
