package com.platform.presentation;

import com.platform.application.*;

import java.util.Scanner;

/**
 * Top-level CLI menu — routes the user to the appropriate role menu.
 */
public class MainMenu {

    private final ClientService clientService;
    private final ConsultantService consultantService;
    private final AdminService adminService;
    private final BookingService bookingService;
    private final PaymentService paymentService;
    private final ServiceCatalogService catalogService;
    private final NotificationService notificationService;
    private final Scanner scanner;

    public MainMenu(ClientService clientService,
                    ConsultantService consultantService,
                    AdminService adminService,
                    BookingService bookingService,
                    PaymentService paymentService,
                    ServiceCatalogService catalogService,
                    NotificationService notificationService) {
        this.clientService = clientService;
        this.consultantService = consultantService;
        this.adminService = adminService;
        this.bookingService = bookingService;
        this.paymentService = paymentService;
        this.catalogService = catalogService;
        this.notificationService = notificationService;
        this.scanner = new Scanner(System.in);
    }

    public void show() {
        System.out.println("╔══════════════════════════════════════════════╗");
        System.out.println("║  Service Booking & Consulting Platform       ║");
        System.out.println("║  EECS3311 Phase 1  —  CLI Demo               ║");
        System.out.println("╚══════════════════════════════════════════════╝");

        boolean running = true;
        while (running) {
            System.out.println("\n=== MAIN MENU ===");
            System.out.println("1. Client menu");
            System.out.println("2. Consultant menu");
            System.out.println("3. Admin menu");
            System.out.println("0. Exit");
            System.out.print("Choose: ");

            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> showClientSelection();
                case "2" -> showConsultantSelection();
                case "3" -> new AdminMenu(adminService, consultantService, scanner).show();
                case "0" -> { running = false; System.out.println("Goodbye!"); }
                default  -> System.out.println("Invalid option.");
            }
        }
    }

    private void showClientSelection() {
        var clients = clientService.getAllClients();
        if (clients.isEmpty()) {
            System.out.println("No clients registered.");
            return;
        }
        System.out.println("\n--- Select client ---");
        for (int i = 0; i < clients.size(); i++) {
            var c = clients.get(i);
            System.out.printf("%d. %s (%s) [ID: %s]%n", i + 1, c.getName(), c.getEmail(), c.getId());
        }
        System.out.print("Choice (or 0 to cancel): ");
        String input = scanner.nextLine().trim();
        if ("0".equals(input)) return;
        try {
            int idx = Integer.parseInt(input) - 1;
            if (idx >= 0 && idx < clients.size()) {
                new ClientMenu(bookingService, paymentService, catalogService,
                        clientService, consultantService, notificationService, scanner)
                        .show(clients.get(idx).getId());
            } else {
                System.out.println("Invalid selection.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid input.");
        }
    }

    private void showConsultantSelection() {
        var consultants = consultantService.getAllConsultants();
        if (consultants.isEmpty()) {
            System.out.println("No consultants registered.");
            return;
        }
        System.out.println("\n--- Select consultant ---");
        for (int i = 0; i < consultants.size(); i++) {
            var c = consultants.get(i);
            System.out.printf("%d. %s [%s] [ID: %s]%n",
                    i + 1, c.getName(), c.getStatus(), c.getId());
        }
        System.out.print("Choice (or 0 to cancel): ");
        String input = scanner.nextLine().trim();
        if ("0".equals(input)) return;
        try {
            int idx = Integer.parseInt(input) - 1;
            if (idx >= 0 && idx < consultants.size()) {
                new ConsultantMenu(bookingService, consultantService, scanner)
                        .show(consultants.get(idx).getId());
            } else {
                System.out.println("Invalid selection.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid input.");
        }
    }
}
