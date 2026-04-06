package com.platform.presentation;

import com.platform.application.*;
import com.platform.domain.policy.*;

import java.util.Scanner;

/**
 * CLI menu for admin operations (UC11, UC12).
 */
public class AdminMenu {

    private final AdminService adminService;
    private final ConsultantService consultantService;
    private final Scanner scanner;

    public AdminMenu(AdminService adminService,
                     ConsultantService consultantService,
                     Scanner scanner) {
        this.adminService = adminService;
        this.consultantService = consultantService;
        this.scanner = scanner;
    }

    public void show() {
        System.out.println("\n=== ADMIN MENU ===");

        boolean running = true;
        while (running) {
            System.out.println("\n1. View all consultants (UC11)");
            System.out.println("2. Approve a consultant (UC11)");
            System.out.println("3. Reject a consultant (UC11)");
            System.out.println("4. View current system policy (UC12)");
            System.out.println("5. Set cancellation policy (UC12)");
            System.out.println("6. Set pricing strategy (UC12)");
            System.out.println("7. Toggle notifications (UC12)");
            System.out.println("0. Back");
            System.out.print("Choose: ");

            String choice = scanner.nextLine().trim();
            try {
                switch (choice) {
                    case "1" -> listConsultants();
                    case "2" -> approveConsultant();
                    case "3" -> rejectConsultant();
                    case "4" -> viewPolicy();
                    case "5" -> setCancellationPolicy();
                    case "6" -> setPricingStrategy();
                    case "7" -> toggleNotifications();
                    case "0" -> running = false;
                    default  -> System.out.println("Invalid option.");
                }
            } catch (Exception e) {
                System.out.println("[ERROR] " + e.getMessage());
            }
        }
    }

    private void listConsultants() {
        var consultants = adminService.getAllConsultants();
        if (consultants.isEmpty()) { System.out.println("No consultants."); return; }
        System.out.println("\n--- All Consultants ---");
        consultants.forEach(c -> System.out.printf("  [%s] %s | Status: %s%n",
                c.getId().substring(0, 8), c.getName(), c.getStatus()));
    }

    private void approveConsultant() {
        listConsultants();
        System.out.print("Enter Consultant ID (or prefix) to approve: ");
        String prefix = scanner.nextLine().trim();
        var consultant = adminService.getAllConsultants().stream()
                .filter(c -> c.getId().startsWith(prefix)).findFirst().orElse(null);
        if (consultant == null) { System.out.println("Not found."); return; }
        adminService.approveConsultant(consultant.getId());
        System.out.println("Consultant approved: " + consultant.getName());
    }

    private void rejectConsultant() {
        listConsultants();
        System.out.print("Enter Consultant ID (or prefix) to reject: ");
        String prefix = scanner.nextLine().trim();
        var consultant = adminService.getAllConsultants().stream()
                .filter(c -> c.getId().startsWith(prefix)).findFirst().orElse(null);
        if (consultant == null) { System.out.println("Not found."); return; }
        adminService.rejectConsultant(consultant.getId());
        System.out.println("Consultant rejected: " + consultant.getName());
    }

    private void viewPolicy() {
        var policy = adminService.getSystemPolicy();
        System.out.println("\n--- Current System Policy ---");
        System.out.println("  Cancellation Policy : " + policy.getCancellationPolicy().getName());
        System.out.println("  Pricing Strategy    : " + policy.getPricingStrategy().getName());
        System.out.println("  Notifications       : " + (policy.isNotificationsEnabled() ? "Enabled" : "Disabled"));
    }

    private void setCancellationPolicy() {
        System.out.println("Select cancellation policy:");
        System.out.println("  1. Free Cancellation (100% refund)");
        System.out.println("  2. Partial Refund (configurable %)");
        System.out.println("  3. No Refund");
        System.out.print("Choice: ");
        String choice = scanner.nextLine().trim();
        switch (choice) {
            case "1" -> adminService.setCancellationPolicy(new FreeCancellationPolicy());
            case "2" -> {
                System.out.print("Refund percent (0-100): ");
                double pct = Double.parseDouble(scanner.nextLine().trim()) / 100.0;
                adminService.setCancellationPolicy(new PartialRefundPolicy(pct));
            }
            case "3" -> adminService.setCancellationPolicy(new NoCancellationRefundPolicy());
            default  -> System.out.println("Invalid choice.");
        }
    }

    private void setPricingStrategy() {
        System.out.println("Select pricing strategy:");
        System.out.println("  1. Base Pricing (no discount)");
        System.out.println("  2. Discounted Pricing");
        System.out.print("Choice: ");
        String choice = scanner.nextLine().trim();
        switch (choice) {
            case "1" -> adminService.setPricingStrategy(new BasePricingStrategy());
            case "2" -> {
                System.out.print("Discount percent (0-100): ");
                double pct = Double.parseDouble(scanner.nextLine().trim()) / 100.0;
                adminService.setPricingStrategy(new DiscountedPricingStrategy(pct));
            }
            default -> System.out.println("Invalid choice.");
        }
    }

    private void toggleNotifications() {
        boolean current = adminService.getSystemPolicy().isNotificationsEnabled();
        adminService.setNotificationsEnabled(!current);
    }
}
