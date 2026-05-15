package com.skybooker.booking.service;

import com.skybooker.booking.dto.BookingRequest;
import com.skybooker.booking.dto.BookingResponse;
import com.skybooker.booking.entity.Booking;
import com.skybooker.booking.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final RestTemplate      restTemplate;

    @Value("${service.flight.url}")
    private String flightServiceUrl;

    @Override
    public BookingResponse bookFlight(BookingRequest request) {

        // Reduce seats in flight-service
        String url = flightServiceUrl + "/flights/" + request.getFlightId()
                + "/reduce-seats?seats=" + request.getSeats();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", request.getToken());

        ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.PUT, new HttpEntity<>(headers), String.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Seat reduction failed: " + response.getStatusCode());
        }

        // Save booking with PENDING status – it becomes CONFIRMED only after payment verification
        Booking booking = new Booking();
        booking.setFlightId(request.getFlightId());
        booking.setUserEmail(request.getUserEmail());
        booking.setSeatsBooked(request.getSeats());
        booking.setTotalPrice(0.0);
        booking.setStatus("PENDING");
        booking.setPnr(generatePnr());
        booking.setBookingTime(LocalDateTime.now());

        Booking saved = bookingRepository.save(booking);

        return new BookingResponse(
                saved.getId(), "Booking created. Complete payment to confirm.",
                true,
                saved.getFlightId(), saved.getUserEmail(),
                saved.getSeatsBooked(), saved.getStatus(), saved.getPnr());
    }

    @Override
    public BookingResponse confirmBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found: " + bookingId));

        if ("CONFIRMED".equals(booking.getStatus())) {
            return mapToResponse(booking, "Booking already confirmed");
        }
        booking.setStatus("CONFIRMED");
        Booking saved = bookingRepository.save(booking);
        return mapToResponse(saved, "Booking confirmed successfully");
    }

    @Override
    public BookingResponse getBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found: " + bookingId));
        return mapToResponse(booking, "Success");
    }

    // ─── helpers ────────────────────────────────────────────────────────────

    /** Generates a human-readable PNR like SKY-A3F9B2 */
    private String generatePnr() {
        return "SKY-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }

    private BookingResponse mapToResponse(Booking b, String message) {
        return new BookingResponse(
                b.getId(), message, true,
                b.getFlightId(), b.getUserEmail(),
                b.getSeatsBooked(), b.getStatus(), b.getPnr());
    }
}
