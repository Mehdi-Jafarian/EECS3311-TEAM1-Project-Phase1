package com.platform.presentation;

import com.platform.application.*;
import com.platform.domain.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;

/**
 * CLI menu for consultant-facing operations (UC8, UC9, UC10).
 */
public class ConsultantMenu {

    private final BookingService bookingService;
    private final ConsultantService consultantService;
    private final Scanner scanner;

    public ConsultantMenu(BookingService bookingService,
                          ConsultantService consultantService,
                          Scanner scanner) {
        this.bookingService = bookingService;
        this.consultantService = consultantService;
        this.scanner = scanner;
    }

    public void show(String consultantId) {
        Consultant consultant = consultantService.getConsultant(consultantId);
        System.out.println("\n=== CONSULTANT MENU: " + consultant.getName()
                + " [" + consultant.getStatus() + "] ===");

        boolean running = true;
        while (running) {
            System.out.println("\n1. Manage availability (UC8)");
            System.out.println("2. View incoming booking requests");
            System.out.println("3. Accept a booking request (UC9)");
            System.out.println("4. Reject a booking request (UC9)");
            System.out.println("5. Complete a booking (UC10)");
            System.out.println("0. Back");
            System.out.print("Choose: ");

            String choice = scanner.nextLine().trim();
            try {
                switch (choice) {
                    case "1" -> manageAvailability(consultantId);
                    case "2" -> viewBookingRequests(consultantId);
                    case "3" -> acceptBooking(consultantId);
                    case "4" -> rejectBooking(consultantId);
                    case "5" -> completeBooking(consultantId);
                    case "0" -> running = false;
                    default  -> System.out.println("Invalid option.");
                }
            } catch (Exception e) {
                System.out.println("[ERROR] " + e.getMessage());
            }
        }
    }

    // ── UC8 ──────────────────────────────────────────────────────────────────
    private void manageAvailability(String consultantId) {
        boolean running = true;
        while (running) {
            System.out.println("\n--- Availability Management ---");
            System.out.println("1. View my time slots");
            System.out.println("2. Add a time slot");
            System.out.println("0. Back");
            System.out.print("Choose: ");
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> viewTimeSlots(consultantId);
                case "2" -> addTimeSlot(consultantId);
                case "0" -> running = false;
                default  -> System.out.println("Invalid.");
            }
        }
    }

    private void viewTimeSlots(String consultantId) {
        var slots = consultantService.getTimeSlots(consultantId);
        if (slots.isEmpty()) { System.out.println("No time slots defined."); return; }
        System.out.println("\n--- My Time Slots ---");
        slots.forEach(s -> System.out.printf(
                "  [%s] %s %s-%s [%s]%n",
                s.getId().substring(0, 8), s.getDate(),
                s.getStartTime(), s.getEndTime(),
                s.isAvailable() ? "Available" : "Booked"));
    }

    private void addTimeSlot(String consultantId) {
        System.out.print("Date (YYYY-MM-DD): ");
        LocalDate date = LocalDate.parse(scanner.nextLine().trim());
        System.out.print("Start time (HH:MM): ");
        LocalTime start = LocalTime.parse(scanner.nextLine().trim());
        System.out.print("End time (HH:MM): ");
        LocalTime end = LocalTime.parse(scanner.nextLine().trim());

        TimeSlot slot = new TimeSlot(UUID.randomUUID().toString(),
                consultantId, date, start, end);
        consultantService.addTimeSlot(consultantId, slot);
        System.out.println("Slot added: " + slot.getId().substring(0, 8));
    }

    // ── UC9 ──────────────────────────────────────────────────────────────────
    private void viewBookingRequests(String consultantId) {
        var bookings = bookingService.getBookingsForConsultant(consultantId);
        if (bookings.isEmpty()) { System.out.println("No bookings found."); return; }
        System.out.println("\n--- All My Bookings ---");
        printBookings(bookings);
    }

    private void acceptBooking(String consultantId) {
        var requested = getBookingsByStatus(consultantId, BookingStatus.REQUESTED);
        if (requested.isEmpty()) { System.out.println("No pending requests."); return; }
        printBookings(requested);
        System.out.print("Enter Booking ID (or prefix) to accept: ");
        String prefix = scanner.nextLine().trim();
        Booking b = findByPrefix(requested, prefix);
        if (b == null) { System.out.println("Not found."); return; }
        bookingService.acceptBooking(b.getId(), consultantId);
        System.out.println("Booking accepted. Client notified to proceed with payment.");
    }

    private void rejectBooking(String consultantId) {
        var requested = getBookingsByStatus(consultantId, BookingStatus.REQUESTED);
        if (requested.isEmpty()) { System.out.println("No pending requests."); return; }
        printBookings(requested);
        System.out.print("Enter Booking ID (or prefix) to reject: ");
        String prefix = scanner.nextLine().trim();
        Booking b = findByPrefix(requested, prefix);
        if (b == null) { System.out.println("Not found."); return; }
        bookingService.rejectBooking(b.getId(), consultantId);
        System.out.println("Booking rejected. Client notified.");
    }

    // ── UC10 ─────────────────────────────────────────────────────────────────
    private void completeBooking(String consultantId) {
        var paid = getBookingsByStatus(consultantId, BookingStatus.PAID);
        if (paid.isEmpty()) { System.out.println("No paid bookings to complete."); return; }
        printBookings(paid);
        System.out.print("Enter Booking ID (or prefix) to complete: ");
        String prefix = scanner.nextLine().trim();
        Booking b = findByPrefix(paid, prefix);
        if (b == null) { System.out.println("Not found."); return; }
        bookingService.completeBooking(b.getId(), consultantId);
        System.out.println("Booking marked as COMPLETED.");
    }

    // ── Helpers ──────────────────────────────────────────────────────────────
    private List<Booking> getBookingsByStatus(String consultantId, BookingStatus status) {
        return bookingService.getBookingsForConsultant(consultantId).stream()
                .filter(b -> b.getStatus() == status).toList();
    }

    private void printBookings(List<Booking> bookings) {
        bookings.forEach(b -> System.out.printf(
                "  [%s] client=%s service=%s status=%s%n",
                b.getId().substring(0, 8),
                b.getClientId().substring(0, 8),
                b.getServiceId().substring(0, 8),
                b.getStatus()));
    }

    private Booking findByPrefix(List<Booking> bookings, String prefix) {
        return bookings.stream().filter(b -> b.getId().startsWith(prefix)).findFirst().orElse(null);
    }
}
