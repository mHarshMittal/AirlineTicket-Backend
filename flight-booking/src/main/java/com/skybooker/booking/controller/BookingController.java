package com.skybooker.booking.controller;

import com.skybooker.booking.dto.BookingRequest;
import com.skybooker.booking.dto.BookingResponse;
import com.skybooker.booking.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    /** Step 1 – Creates booking with PENDING status, reduces seats. */
    @PostMapping
    public BookingResponse bookFlight(
            @RequestBody BookingRequest request,
            @RequestHeader("Authorization") String token) {
        request.setToken(token);
        return bookingService.bookFlight(request);
    }

    /**
     * Step 2 – Called by payment-service (internal) after payment is verified.
     * Flips booking status from PENDING → CONFIRMED.
     */
    @PutMapping("/{bookingId}/confirm")
    public ResponseEntity<BookingResponse> confirmBooking(@PathVariable Long bookingId) {
        return ResponseEntity.ok(bookingService.confirmBooking(bookingId));
    }

    /** Used by payment-service to fetch flightId + pnr for the confirmation email. */
    @GetMapping("/{bookingId}")
    public ResponseEntity<BookingResponse> getBooking(@PathVariable Long bookingId) {
        return ResponseEntity.ok(bookingService.getBooking(bookingId));
    }
}
