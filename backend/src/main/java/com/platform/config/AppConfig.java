package com.platform.config;

import com.platform.application.*;
import com.platform.application.payment.PaymentProcessorFactory;
import com.platform.domain.TimeSlot;
import com.platform.domain.policy.SystemPolicy;
import com.platform.infrastructure.SystemTimeProvider;
import com.platform.infrastructure.TimeProvider;
import com.platform.infrastructure.repository.*;
import com.platform.infrastructure.repository.jdbc.*;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Configuration
public class AppConfig {

    @Bean
    public TimeProvider timeProvider() {
        return new SystemTimeProvider();
    }

    @Bean
    public SystemPolicy systemPolicy() {
        return new SystemPolicy();
    }

    // --- JDBC repositories (when DB_HOST is set) ---

    @Bean
    @ConditionalOnProperty(name = "DB_HOST")
    public BookingRepository jdbcBookingRepository(JdbcTemplate jdbc) {
        return new JdbcBookingRepository(jdbc);
    }

    @Bean
    @ConditionalOnProperty(name = "DB_HOST")
    public TimeSlotRepository jdbcTimeSlotRepository(JdbcTemplate jdbc) {
        return new JdbcTimeSlotRepository(jdbc);
    }

    @Bean
    @ConditionalOnProperty(name = "DB_HOST")
    public ServiceRepository jdbcServiceRepository(JdbcTemplate jdbc) {
        return new JdbcServiceRepository(jdbc);
    }

    @Bean
    @ConditionalOnProperty(name = "DB_HOST")
    public ConsultantRepository jdbcConsultantRepository(JdbcTemplate jdbc) {
        return new JdbcConsultantRepository(jdbc);
    }

    @Bean
    @ConditionalOnProperty(name = "DB_HOST")
    public ClientRepository jdbcClientRepository(JdbcTemplate jdbc) {
        return new JdbcClientRepository(jdbc);
    }

    @Bean
    @ConditionalOnProperty(name = "DB_HOST")
    public PaymentRepository jdbcPaymentRepository(JdbcTemplate jdbc) {
        return new JdbcPaymentRepository(jdbc);
    }

    @Bean
    @ConditionalOnProperty(name = "DB_HOST")
    public PaymentMethodRepository jdbcPaymentMethodRepository(JdbcTemplate jdbc) {
        return new JdbcPaymentMethodRepository(jdbc);
    }

    @Bean
    @ConditionalOnProperty(name = "DB_HOST")
    public NotificationRepository jdbcNotificationRepository(JdbcTemplate jdbc) {
        return new JdbcNotificationRepository(jdbc);
    }

    // --- InMemory repositories (fallback when no DB) ---

    @Bean
    @ConditionalOnMissingBean(BookingRepository.class)
    public BookingRepository inMemoryBookingRepository() {
        return new InMemoryBookingRepository();
    }

    @Bean
    @ConditionalOnMissingBean(TimeSlotRepository.class)
    public TimeSlotRepository inMemoryTimeSlotRepository() {
        return new InMemoryTimeSlotRepository();
    }

    @Bean
    @ConditionalOnMissingBean(ServiceRepository.class)
    public ServiceRepository inMemoryServiceRepository() {
        return new InMemoryServiceRepository();
    }

    @Bean
    @ConditionalOnMissingBean(ConsultantRepository.class)
    public ConsultantRepository inMemoryConsultantRepository() {
        return new InMemoryConsultantRepository();
    }

    @Bean
    @ConditionalOnMissingBean(ClientRepository.class)
    public ClientRepository inMemoryClientRepository() {
        return new InMemoryClientRepository();
    }

    @Bean
    @ConditionalOnMissingBean(PaymentRepository.class)
    public PaymentRepository inMemoryPaymentRepository() {
        return new InMemoryPaymentRepository();
    }

    @Bean
    @ConditionalOnMissingBean(PaymentMethodRepository.class)
    public PaymentMethodRepository inMemoryPaymentMethodRepository() {
        return new InMemoryPaymentMethodRepository();
    }

    @Bean
    @ConditionalOnMissingBean(NotificationRepository.class)
    public NotificationRepository inMemoryNotificationRepository() {
        return new InMemoryNotificationRepository();
    }

    // --- Application services ---

    @Bean
    public NotificationService notificationService(NotificationRepository notifRepo, TimeProvider timeProvider) {
        return new NotificationService(notifRepo, timeProvider);
    }

    @Bean
    public BookingService bookingService(BookingRepository bookingRepo, TimeSlotRepository timeSlotRepo,
                                          ServiceRepository serviceRepo, ConsultantRepository consultantRepo,
                                          ClientRepository clientRepo, PaymentRepository paymentRepo,
                                          SystemPolicy systemPolicy, TimeProvider timeProvider) {
        return new BookingService(bookingRepo, timeSlotRepo, serviceRepo, consultantRepo,
                clientRepo, paymentRepo, systemPolicy, timeProvider);
    }

    @Bean
    public PaymentProcessorFactory paymentProcessorFactory(TimeProvider timeProvider) {
        return new PaymentProcessorFactory(timeProvider);
    }

    @Bean
    public PaymentService paymentService(PaymentRepository paymentRepo, PaymentMethodRepository pmRepo,
                                          BookingService bookingService, PaymentProcessorFactory processorFactory,
                                          SystemPolicy systemPolicy) {
        return new PaymentService(paymentRepo, pmRepo, bookingService, processorFactory, systemPolicy);
    }

    @Bean
    public ClientService clientService(ClientRepository clientRepo) {
        return new ClientService(clientRepo);
    }

    @Bean
    public ConsultantService consultantService(ConsultantRepository consultantRepo, TimeSlotRepository timeSlotRepo) {
        return new ConsultantService(consultantRepo, timeSlotRepo);
    }

    @Bean
    public ServiceCatalogService serviceCatalogService(ServiceRepository serviceRepo, SystemPolicy systemPolicy) {
        return new ServiceCatalogService(serviceRepo, systemPolicy);
    }

    @Bean
    public AdminService adminService(ConsultantRepository consultantRepo, SystemPolicy systemPolicy,
                                      NotificationService notificationService) {
        return new AdminService(consultantRepo, systemPolicy, notificationService);
    }

    @Bean
    public ChatbotService chatbotService(@Value("${AI_API_KEY:}") String apiKey,
                                          ServiceCatalogService catalogService, SystemPolicy systemPolicy) {
        return new ChatbotService(apiKey, catalogService, systemPolicy);
    }

    @Bean
    public NotificationObserver notificationObserver(NotificationService notificationService,
                                                      BookingService bookingService, PaymentService paymentService) {
        NotificationObserver observer = new NotificationObserver(notificationService);
        bookingService.addObserver(observer);
        paymentService.addObserver(observer);
        return observer;
    }

    @Bean
    public CommandLineRunner seedDemoData(ClientService clientService, ConsultantService consultantService,
                                           ServiceCatalogService catalogService, AdminService adminService,
                                           @Value("${DB_HOST:}") String dbHost) {
        return args -> {
            if (!dbHost.isBlank()) {
                System.out.println("[SEED] Using database — seed data loaded via init.sql");
                return;
            }
            catalogService.createService("Career Coaching", "Coaching", 60, 150.0);
            catalogService.createService("Tech Architecture Review", "Technical", 90, 250.0);
            catalogService.createService("Business Strategy", "Strategy", 120, 300.0);

            clientService.registerClient("Alice Johnson", "alice@example.com");
            clientService.registerClient("Bob Smith", "bob@example.com");

            var consultant = consultantService.registerConsultant("Dr. Carol White", "carol@example.com");
            adminService.approveConsultant(consultant.getId());

            var tomorrow = LocalDate.now().plusDays(1);
            consultantService.addTimeSlot(consultant.getId(), new TimeSlot(
                    UUID.randomUUID().toString(), consultant.getId(), tomorrow,
                    LocalTime.of(9, 0), LocalTime.of(10, 0)));
            consultantService.addTimeSlot(consultant.getId(), new TimeSlot(
                    UUID.randomUUID().toString(), consultant.getId(), tomorrow,
                    LocalTime.of(11, 0), LocalTime.of(12, 0)));

            System.out.println("[SEED] Demo data loaded (in-memory). Consultant ID: " + consultant.getId());
        };
    }
}
