package com.platform.presentation;

import com.platform.application.*;
import com.platform.domain.*;

import java.util.List;
import java.util.Scanner;
import java.util.UUID;

/**
 * CLI menu for client-facing operations (UC1–UC7).
 * All business logic is delegated to application services.
 */
public class ClientMenu {

    private final BookingService bookingService;
    private final PaymentService paymentService;
    private final ServiceCatalogService catalogService;
    private final ClientService clientService;
    private final ConsultantService consultantService;
    private final NotificationService notificationService;
    private final Scanner scanner;

    public ClientMenu(BookingService bookingService,
                      PaymentService paymentService,
                      ServiceCatalogService catalogService,
                      ClientService clientService,
                      ConsultantService consultantService,
                      NotificationService notificationService,
                      Scanner scanner) {
        this.bookingService = bookingService;
        this.paymentService = paymentService;
        this.catalogService = catalogService;
        this.clientService = clientService;
        this.consultantService = consultantService;
        this.notificationService = notificationService;
        this.scanner = scanner;
    }

    public void show(String clientId) {
        Client client = clientService.getClient(clientId);
        System.out.println("\n=== CLIENT MENU: " + client.getName() + " ===");

        boolean running = true;
        while (running) {
            System.out.println("\n1. Browse consulting services (UC1)");
            System.out.println("2. Request a booking (UC2)");
            System.out.println("3. Cancel a booking (UC3)");
            System.out.println("4. View booking history (UC4)");
            System.out.println("5. Process payment (UC5)");
            System.out.println("6. Manage payment methods (UC6)");
            System.out.println("7. View payment history (UC7)");
            System.out.println("8. View my notifications");
            System.out.println("0. Back");
            System.out.print("Choose: ");

            String choice = scanner.nextLine().trim();
            try {
                switch (choice) {
                    case "1" -> browseServices();
                    case "2" -> requestBooking(clientId);
                    case "3" -> cancelBooking(clientId);
                    case "4" -> viewBookingHistory(clientId);
                    case "5" -> processPayment(clientId);
                    case "6" -> managePaymentMethods(clientId);
                    case "7" -> viewPaymentHistory(clientId);
                    case "8" -> viewNotifications(clientId);
                    case "0" -> running = false;
                    default  -> System.out.println("Invalid option.");
                }
            } catch (Exception e) {
                System.out.println("[ERROR] " + e.getMessage());
            }
        }
    }

    // ── UC1 ──────────────────────────────────────────────────────────────────
    private void browseServices() {
        List<ConsultingService> services = catalogService.listServices();
        if (services.isEmpty()) {
            System.out.println("No services available.");
            return;
        }
        System.out.println("\n--- Consulting Services ---");
        for (ConsultingService s : services) {
            double price = catalogService.getPriceFor(s.getId());
            System.out.printf("  [%s] %s | Type: %s | Duration: %d min | Price: $%.2f%n",
                    s.getId().substring(0, 8), s.getName(), s.getType(),
                    s.getDurationMinutes(), price);
        }
    }

    // ── UC2 ──────────────────────────────────────────────────────────────────
    private void requestBooking(String clientId) {
        browseServices();

        List<ConsultingService> services = catalogService.listServices();
        if (services.isEmpty()) return;

        System.out.print("Enter Service ID (or prefix): ");
        String servicePrefix = scanner.nextLine().trim();
        ConsultingService service = findByPrefix(services, servicePrefix);
        if (service == null) { System.out.println("Service not found."); return; }

        // Show consultants
        List<Consultant> consultants = consultantService.getAllConsultants().stream()
                .filter(Consultant::isApproved).toList();
        if (consultants.isEmpty()) { System.out.println("No approved consultants."); return; }
        System.out.println("\n--- Approved Consultants ---");
        consultants.forEach(c -> System.out.printf("  [%s] %s%n",
                c.getId().substring(0, 8), c.getName()));

        System.out.print("Enter Consultant ID (or prefix): ");
        String consultantPrefix = scanner.nextLine().trim();
        Consultant consultant = consultants.stream()
                .filter(c -> c.getId().startsWith(consultantPrefix))
                .findFirst().orElse(null);
        if (consultant == null) { System.out.println("Consultant not found."); return; }

        // Show available slots
        List<TimeSlot> slots = consultantService.getTimeSlots(consultant.getId()).stream()
                .filter(TimeSlot::isAvailable).toList();
        if (slots.isEmpty()) { System.out.println("No available slots."); return; }
        System.out.println("\n--- Available Time Slots ---");
        slots.forEach(s -> System.out.printf("  [%s] %s %s-%s%n",
                s.getId().substring(0, 8), s.getDate(), s.getStartTime(), s.getEndTime()));

        System.out.print("Enter Slot ID (or prefix): ");
        String slotPrefix = scanner.nextLine().trim();
        TimeSlot slot = slots.stream()
                .filter(s -> s.getId().startsWith(slotPrefix))
                .findFirst().orElse(null);
        if (slot == null) { System.out.println("Slot not found."); return; }

        Booking booking = bookingService.requestBooking(
                clientId, consultant.getId(), service.getId(), slot.getId());
        System.out.println("Booking created: " + booking.getId());
        System.out.println("Status: " + booking.getStatus());
    }

    // ── UC3 ──────────────────────────────────────────────────────────────────
    private void cancelBooking(String clientId) {
        List<Booking> bookings = bookingService.getBookingsForClient(clientId);
        if (bookings.isEmpty()) { System.out.println("No bookings found."); return; }

        printBookings(bookings);
        System.out.print("Enter Booking ID (or prefix) to cancel: ");
        String prefix = scanner.nextLine().trim();
        Booking booking = bookings.stream()
                .filter(b -> b.getId().startsWith(prefix)).findFirst().orElse(null);
        if (booking == null) { System.out.println("Booking not found."); return; }

        double refund = bookingService.cancelBooking(booking.getId(), clientId);
        System.out.printf("Booking cancelled. Refund: $%.2f%n", refund);
    }

    // ── UC4 ──────────────────────────────────────────────────────────────────
    private void viewBookingHistory(String clientId) {
        List<Booking> bookings = bookingService.getBookingsForClient(clientId);
        if (bookings.isEmpty()) { System.out.println("No bookings found."); return; }
        System.out.println("\n--- Booking History ---");
        printBookings(bookings);
    }

    // ── UC5 ──────────────────────────────────────────────────────────────────
    private void processPayment(String clientId) {
        List<Booking> pending = bookingService.getBookingsForClient(clientId).stream()
                .filter(b -> b.getStatus() == BookingStatus.PENDING_PAYMENT).toList();
        if (pending.isEmpty()) { System.out.println("No bookings awaiting payment."); return; }

        System.out.println("\n--- Bookings Awaiting Payment ---");
        printBookings(pending);
        System.out.print("Enter Booking ID (or prefix): ");
        String bPrefix = scanner.nextLine().trim();
        Booking booking = pending.stream()
                .filter(b -> b.getId().startsWith(bPrefix)).findFirst().orElse(null);
        if (booking == null) { System.out.println("Booking not found."); return; }

        // Select payment method
        List<PaymentMethod> methods = paymentService.getPaymentMethods(clientId);
        PaymentMethod method;
        if (methods.isEmpty()) {
            System.out.println("No saved payment methods. Please add one first (option 6).");
            return;
        }
        System.out.println("\n--- Saved Payment Methods ---");
        for (int i = 0; i < methods.size(); i++) {
            System.out.printf("%d. %s%n", i + 1, methods.get(i));
        }
        System.out.print("Choose payment method: ");
        try {
            int idx = Integer.parseInt(scanner.nextLine().trim()) - 1;
            if (idx < 0 || idx >= methods.size()) { System.out.println("Invalid."); return; }
            method = methods.get(idx);
        } catch (NumberFormatException e) { System.out.println("Invalid."); return; }

        System.out.print("Enter amount to pay: $");
        double amount;
        try {
            amount = Double.parseDouble(scanner.nextLine().trim());
        } catch (NumberFormatException e) { System.out.println("Invalid amount."); return; }

        System.out.println("Processing payment... (please wait)");
        Payment payment = paymentService.processPayment(booking.getId(), method.getId(), amount);
        System.out.println("Payment successful!");
        System.out.println("Transaction ID: " + payment.getTransactionId());
        System.out.printf("Amount charged: $%.2f%n", payment.getAmount());
    }

    // ── UC6 ──────────────────────────────────────────────────────────────────
    private void managePaymentMethods(String clientId) {
        boolean running = true;
        while (running) {
            System.out.println("\n--- Payment Methods ---");
            System.out.println("1. View saved methods");
            System.out.println("2. Add credit card");
            System.out.println("3. Add debit card");
            System.out.println("4. Add PayPal");
            System.out.println("5. Add bank transfer");
            System.out.println("6. Remove a method");
            System.out.println("0. Back");
            System.out.print("Choose: ");
            String choice = scanner.nextLine().trim();
            try {
                switch (choice) {
                    case "1" -> listPaymentMethods(clientId);
                    case "2" -> addCard(clientId, false);
                    case "3" -> addCard(clientId, true);
                    case "4" -> addPayPal(clientId);
                    case "5" -> addBankTransfer(clientId);
                    case "6" -> removePaymentMethod(clientId);
                    case "0" -> running = false;
                    default  -> System.out.println("Invalid.");
                }
            } catch (Exception e) {
                System.out.println("[ERROR] " + e.getMessage());
            }
        }
    }

    private void listPaymentMethods(String clientId) {
        var methods = paymentService.getPaymentMethods(clientId);
        if (methods.isEmpty()) { System.out.println("No saved methods."); return; }
        methods.forEach(m -> System.out.println("  " + m));
    }

    private void addCard(String clientId, boolean isDebit) {
        System.out.print("Card number (16 digits): ");
        String cardNumber = scanner.nextLine().trim();
        System.out.print("Expiry month (1-12): ");
        int month = Integer.parseInt(scanner.nextLine().trim());
        System.out.print("Expiry year (e.g. 2027): ");
        int year = Integer.parseInt(scanner.nextLine().trim());
        System.out.print("CVV (3-4 digits): ");
        String cvv = scanner.nextLine().trim();
        String id = UUID.randomUUID().toString();
        PaymentMethod pm = isDebit
                ? new com.platform.domain.DebitCardPaymentMethod(id, clientId, cardNumber, month, year, cvv)
                : new com.platform.domain.CreditCardPaymentMethod(id, clientId, cardNumber, month, year, cvv);
        paymentService.addPaymentMethod(pm);
        System.out.println((isDebit ? "Debit" : "Credit") + " card saved (ID: " + id.substring(0, 8) + ").");
    }

    private void addPayPal(String clientId) {
        System.out.print("PayPal email: ");
        String email = scanner.nextLine().trim();
        String id = UUID.randomUUID().toString();
        PaymentMethod pm = new com.platform.domain.PayPalPaymentMethod(id, clientId, email);
        paymentService.addPaymentMethod(pm);
        System.out.println("PayPal method saved (ID: " + id.substring(0, 8) + ").");
    }

    private void addBankTransfer(String clientId) {
        System.out.print("Account number (8-17 digits): ");
        String account = scanner.nextLine().trim();
        System.out.print("Routing number (9 digits): ");
        String routing = scanner.nextLine().trim();
        String id = UUID.randomUUID().toString();
        PaymentMethod pm = new com.platform.domain.BankTransferPaymentMethod(id, clientId, account, routing);
        paymentService.addPaymentMethod(pm);
        System.out.println("Bank transfer method saved (ID: " + id.substring(0, 8) + ").");
    }

    private void removePaymentMethod(String clientId) {
        listPaymentMethods(clientId);
        System.out.print("Enter method ID (or prefix) to remove: ");
        String prefix = scanner.nextLine().trim();
        var methods = paymentService.getPaymentMethods(clientId);
        var pm = methods.stream().filter(m -> m.getId().startsWith(prefix)).findFirst().orElse(null);
        if (pm == null) { System.out.println("Method not found."); return; }
        paymentService.removePaymentMethod(pm.getId(), clientId);
        System.out.println("Payment method removed.");
    }

    // ── UC7 ──────────────────────────────────────────────────────────────────
    private void viewPaymentHistory(String clientId) {
        var payments = paymentService.getPaymentHistory(clientId);
        if (payments.isEmpty()) { System.out.println("No payment history."); return; }
        System.out.println("\n--- Payment History ---");
        payments.forEach(p -> System.out.printf(
                "  Booking: %s | TxnID: %s | Amount: $%.2f | Type: %s | Status: %s | Date: %s%n",
                p.getBookingId().substring(0, 8),
                p.getTransactionId().substring(0, 16),
                p.getAmount(), p.getPaymentType(), p.getStatus(), p.getPaidAt()));
    }

    // ── Notifications ─────────────────────────────────────────────────────────
    private void viewNotifications(String clientId) {
        var notifs = notificationService.getNotifications(clientId);
        if (notifs.isEmpty()) { System.out.println("No notifications."); return; }
        System.out.println("\n--- Notifications ---");
        notifs.forEach(System.out::println);
    }

    // ── Helpers ──────────────────────────────────────────────────────────────
    private void printBookings(List<Booking> bookings) {
        bookings.forEach(b -> System.out.printf(
                "  [%s] consultant=%s service=%s slot=%s status=%s created=%s%n",
                b.getId().substring(0, 8),
                b.getConsultantId().substring(0, 8),
                b.getServiceId().substring(0, 8),
                b.getTimeSlotId().substring(0, 8),
                b.getStatus(),
                b.getCreatedAt().toLocalDate()));
    }

    private ConsultingService findByPrefix(List<ConsultingService> services, String prefix) {
        return services.stream()
                .filter(s -> s.getId().startsWith(prefix))
                .findFirst().orElse(null);
    }
}
