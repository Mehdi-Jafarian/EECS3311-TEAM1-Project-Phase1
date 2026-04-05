package com.platform.presentation.api;

import com.platform.application.BookingService;
import com.platform.domain.Booking;

import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    public Booking requestBooking(@RequestBody Map<String, String> body) {
        return bookingService.requestBooking(
                body.get("clientId"),
                body.get("consultantId"),
                body.get("serviceId"),
                body.get("slotId")
        );
    }

    @GetMapping("/client/{clientId}")
    public List<Booking> getBookingsForClient(@PathVariable String clientId) {
        return bookingService.getBookingsForClient(clientId);
    }

    @GetMapping("/consultant/{consultantId}")
    public List<Booking> getBookingsForConsultant(@PathVariable String consultantId) {
        return bookingService.getBookingsForConsultant(consultantId);
    }

    @PutMapping("/{id}/accept")
    public void acceptBooking(@PathVariable String id, @RequestParam String consultantId) {
        bookingService.acceptBooking(id, consultantId);
    }

    @PutMapping("/{id}/reject")
    public void rejectBooking(@PathVariable String id, @RequestParam String consultantId) {
        bookingService.rejectBooking(id, consultantId);
    }

    @PutMapping("/{id}/complete")
    public void completeBooking(@PathVariable String id, @RequestParam String consultantId) {
        bookingService.completeBooking(id, consultantId);
    }

    @PutMapping("/{id}/cancel")
    public Map<String, Object> cancelBooking(@PathVariable String id, @RequestParam String clientId) {
        double refund = bookingService.cancelBooking(id, clientId);
        return Map.of("bookingId", id, "refundAmount", refund);
    }
}
