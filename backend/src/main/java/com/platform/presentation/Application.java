package com.platform.presentation;

import com.platform.application.*;
import com.platform.application.payment.PaymentProcessorFactory;
import com.platform.domain.*;
import com.platform.domain.policy.SystemPolicy;
import com.platform.infrastructure.*;
import com.platform.infrastructure.repository.*;

/**
 * Entry point — wires all components together and launches the CLI.
 *
 * <p>All wiring is done manually (no DI framework per project constraints).
 */
public class Application {

    public static void main(String[] args) {
        // ── Infrastructure ────────────────────────────────────────────────
        TimeProvider timeProvider = new SystemTimeProvider();

        BookingRepository bookingRepo        = new InMemoryBookingRepository();
        TimeSlotRepository timeSlotRepo      = new InMemoryTimeSlotRepository();
        ServiceRepository serviceRepo        = new InMemoryServiceRepository();
        ConsultantRepository consultantRepo  = new InMemoryConsultantRepository();
        ClientRepository clientRepo          = new InMemoryClientRepository();
        PaymentRepository paymentRepo        = new InMemoryPaymentRepository();
        PaymentMethodRepository pmRepo       = new InMemoryPaymentMethodRepository();
        NotificationRepository notifRepo     = new InMemoryNotificationRepository();

        // ── Domain policies ───────────────────────────────────────────────
        SystemPolicy systemPolicy = new SystemPolicy();

        // ── Application services ──────────────────────────────────────────
        NotificationService notificationService =
                new NotificationService(notifRepo, timeProvider);

        BookingService bookingService = new BookingService(
                bookingRepo, timeSlotRepo, serviceRepo, consultantRepo,
                clientRepo, paymentRepo, systemPolicy, timeProvider);

        PaymentProcessorFactory processorFactory =
                new PaymentProcessorFactory(timeProvider); // 2500ms delay

        PaymentService paymentService = new PaymentService(
                paymentRepo, pmRepo, bookingService, processorFactory, systemPolicy);

        ClientService clientService     = new ClientService(clientRepo);
        ConsultantService consultantService =
                new ConsultantService(consultantRepo, timeSlotRepo);
        ServiceCatalogService catalogService =
                new ServiceCatalogService(serviceRepo, systemPolicy);
        AdminService adminService =
                new AdminService(consultantRepo, systemPolicy, notificationService);

        // ── Observer wiring (Observer Pattern) ────────────────────────────
        NotificationObserver notifObserver = new NotificationObserver(notificationService);
        bookingService.addObserver(notifObserver);
        paymentService.addObserver(notifObserver);

        // ── Seed demo data ────────────────────────────────────────────────
        seedDemoData(clientService, consultantService, catalogService, adminService);

        // ── Launch CLI ────────────────────────────────────────────────────
        MainMenu mainMenu = new MainMenu(
                clientService, consultantService, adminService,
                bookingService, paymentService, catalogService,
                notificationService);
        mainMenu.show();
    }

    /** Seeds a minimal set of demo data so the CLI is immediately usable. */
    private static void seedDemoData(ClientService clientService,
                                     ConsultantService consultantService,
                                     ServiceCatalogService catalogService,
                                     AdminService adminService) {
        // Services
        catalogService.createService("Career Coaching", "Coaching", 60, 150.0);
        catalogService.createService("Tech Architecture Review", "Technical", 90, 250.0);
        catalogService.createService("Business Strategy", "Strategy", 120, 300.0);

        // Clients
        clientService.registerClient("Alice Johnson", "alice@example.com");
        clientService.registerClient("Bob Smith", "bob@example.com");

        // Consultant (starts as PENDING — admin must approve)
        var consultant = consultantService.registerConsultant("Dr. Carol White", "carol@example.com");
        System.out.println("[SEED] Consultant registered (PENDING): " + consultant.getId());

        // Auto-approve for demo convenience
        adminService.approveConsultant(consultant.getId());

        // Time slots (added after approval)
        var tomorrow = java.time.LocalDate.now().plusDays(1);
        var slotA = new com.platform.domain.TimeSlot(
                java.util.UUID.randomUUID().toString(),
                consultant.getId(),
                tomorrow,
                java.time.LocalTime.of(9, 0),
                java.time.LocalTime.of(10, 0));
        var slotB = new com.platform.domain.TimeSlot(
                java.util.UUID.randomUUID().toString(),
                consultant.getId(),
                tomorrow,
                java.time.LocalTime.of(11, 0),
                java.time.LocalTime.of(12, 0));
        consultantService.addTimeSlot(consultant.getId(), slotA);
        consultantService.addTimeSlot(consultant.getId(), slotB);

        System.out.println("[SEED] Demo data loaded. Consultant ID: " + consultant.getId());
        System.out.println("[SEED] Slots: " + slotA.getId() + ", " + slotB.getId());
        System.out.println();
    }
}
