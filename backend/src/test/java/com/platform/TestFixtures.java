package com.platform;

import com.platform.application.*;
import com.platform.application.payment.PaymentProcessorFactory;
import com.platform.domain.*;
import com.platform.domain.policy.SystemPolicy;
import com.platform.infrastructure.FixedTimeProvider;
import com.platform.infrastructure.TimeProvider;
import com.platform.infrastructure.repository.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

/**
 * Factory / builder helpers to create pre-wired service graphs for tests.
 * Uses {@link FixedTimeProvider} and 0-ms payment delay for fast tests.
 */
public class TestFixtures {

    public static final LocalDateTime FIXED_NOW = LocalDateTime.of(2030, 6, 15, 10, 0);

    // ── Infrastructure ────────────────────────────────────────────────────

    public static TimeProvider timeProvider() {
        return new FixedTimeProvider(FIXED_NOW);
    }

    // ── Repo instances ────────────────────────────────────────────────────

    public static BookingRepository bookingRepo()             { return new InMemoryBookingRepository(); }
    public static TimeSlotRepository timeSlotRepo()           { return new InMemoryTimeSlotRepository(); }
    public static ServiceRepository serviceRepo()             { return new InMemoryServiceRepository(); }
    public static ConsultantRepository consultantRepo()       { return new InMemoryConsultantRepository(); }
    public static ClientRepository clientRepo()               { return new InMemoryClientRepository(); }
    public static PaymentRepository paymentRepo()             { return new InMemoryPaymentRepository(); }
    public static PaymentMethodRepository paymentMethodRepo() { return new InMemoryPaymentMethodRepository(); }
    public static NotificationRepository notifRepo()          { return new InMemoryNotificationRepository(); }

    // ── Full service graph ─────────────────────────────────────────────────

    public record Services(
            BookingService booking,
            PaymentService payment,
            ClientService client,
            ConsultantService consultant,
            ServiceCatalogService catalog,
            AdminService admin,
            NotificationService notification,
            SystemPolicy policy,
            // repos for direct inspection
            BookingRepository bookingRepo,
            PaymentRepository paymentRepo,
            PaymentMethodRepository pmRepo,
            ConsultantRepository consultantRepo,
            ClientRepository clientRepo) {}

    public static Services buildServices() {
        TimeProvider tp = timeProvider();
        SystemPolicy policy = new SystemPolicy();

        var bookingRepo  = bookingRepo();
        var tsRepo       = timeSlotRepo();
        var serviceRepo  = serviceRepo();
        var consultRepo  = consultantRepo();
        var clientRepo   = clientRepo();
        var payRepo      = paymentRepo();
        var pmRepo       = paymentMethodRepo();
        var notifRepo    = notifRepo();

        var notifSvc = new NotificationService(notifRepo, tp);
        var bookingSvc = new BookingService(bookingRepo, tsRepo, serviceRepo,
                consultRepo, clientRepo, payRepo, policy, tp);
        // 0-ms delay for fast tests
        var processorFactory = new PaymentProcessorFactory(0L, tp);
        var paymentSvc = new PaymentService(payRepo, pmRepo, bookingSvc, processorFactory, policy);
        var clientSvc = new ClientService(clientRepo);
        var consultantSvc = new ConsultantService(consultRepo, tsRepo);
        var catalogSvc = new ServiceCatalogService(serviceRepo, policy);
        var adminSvc = new AdminService(consultRepo, policy, notifSvc);

        // wire Observer
        var notifObserver = new NotificationObserver(notifSvc);
        bookingSvc.addObserver(notifObserver);
        paymentSvc.addObserver(notifObserver);

        return new Services(bookingSvc, paymentSvc, clientSvc, consultantSvc,
                catalogSvc, adminSvc, notifSvc, policy,
                bookingRepo, payRepo, pmRepo, consultRepo, clientRepo);
    }

    // ── Domain object factories ───────────────────────────────────────────

    public static Client client(String name) {
        return new Client(UUID.randomUUID().toString(), name, name.toLowerCase() + "@test.com");
    }

    public static Consultant consultant(String name) {
        return new Consultant(UUID.randomUUID().toString(), name, name.toLowerCase() + "@test.com");
    }

    public static ConsultingService service(String name, double price) {
        return new ConsultingService(UUID.randomUUID().toString(), name, "General", 60, price);
    }

    public static TimeSlot futureSlot(String consultantId) {
        return new TimeSlot(UUID.randomUUID().toString(), consultantId,
                LocalDate.of(2030, 7, 1),
                LocalTime.of(9, 0), LocalTime.of(10, 0));
    }

    /** Seeds a client, approved consultant, service, and available slot into the given services. */
    public static SeedResult seed(Services svc) {
        Client c = svc.client().registerClient("Test Client", "client@test.com");
        Consultant con = svc.consultant().registerConsultant("Test Consultant", "con@test.com");
        svc.admin().approveConsultant(con.getId());
        ConsultingService srv = svc.catalog().createService("Test Service", "General", 60, 100.0);
        TimeSlot slot = futureSlot(con.getId());
        svc.consultant().addTimeSlot(con.getId(), slot);
        return new SeedResult(c, con, srv, slot);
    }

    public record SeedResult(Client client, Consultant consultant,
                              ConsultingService service, TimeSlot slot) {}
}
