package com.platform.presentation.api;

import com.platform.application.PaymentService;
import com.platform.domain.*;

import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/payments")
    public Payment processPayment(@RequestBody Map<String, Object> body) {
        String bookingId = (String) body.get("bookingId");
        String paymentMethodId = (String) body.get("paymentMethodId");
        double amount = ((Number) body.get("amount")).doubleValue();
        return paymentService.processPayment(bookingId, paymentMethodId, amount);
    }

    @GetMapping("/payments/client/{clientId}")
    public List<Payment> getPaymentHistory(@PathVariable String clientId) {
        return paymentService.getPaymentHistory(clientId);
    }

    @PostMapping("/payment-methods")
    public PaymentMethod addPaymentMethod(@RequestBody Map<String, Object> body) {
        String clientId = (String) body.get("clientId");
        String typeStr = (String) body.get("paymentType");
        PaymentType type = PaymentType.valueOf(typeStr);
        String id = UUID.randomUUID().toString();

        PaymentMethod method = switch (type) {
            case CREDIT_CARD -> {
                String expiryDate = (String) body.get("expiryDate");
                String[] parts = expiryDate.split("-");
                int year = Integer.parseInt(parts[0]);
                int month = Integer.parseInt(parts[1]);
                yield new CreditCardPaymentMethod(id, clientId,
                        (String) body.get("cardNumber"), month, year,
                        (String) body.get("cvv"));
            }
            case DEBIT_CARD -> {
                String expiryDate = (String) body.get("expiryDate");
                String[] parts = expiryDate.split("-");
                int year = Integer.parseInt(parts[0]);
                int month = Integer.parseInt(parts[1]);
                yield new DebitCardPaymentMethod(id, clientId,
                        (String) body.get("cardNumber"), month, year,
                        (String) body.get("cvv"));
            }
            case PAYPAL -> new PayPalPaymentMethod(id, clientId,
                    (String) body.get("email"));
            case BANK_TRANSFER -> new BankTransferPaymentMethod(id, clientId,
                    (String) body.get("accountNumber"),
                    (String) body.get("routingNumber"));
        };

        paymentService.addPaymentMethod(method);
        return method;
    }

    @GetMapping("/payment-methods/client/{clientId}")
    public List<PaymentMethod> getPaymentMethods(@PathVariable String clientId) {
        return paymentService.getPaymentMethods(clientId);
    }

    @DeleteMapping("/payment-methods/{id}")
    public void removePaymentMethod(@PathVariable String id, @RequestParam String clientId) {
        paymentService.removePaymentMethod(id, clientId);
    }
}
