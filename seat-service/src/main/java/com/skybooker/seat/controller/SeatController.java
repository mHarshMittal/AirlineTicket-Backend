package com.skybooker.seat.controller;

import com.skybooker.seat.dto.SeatRequest;
import com.skybooker.seat.dto.SeatResponse;
import com.skybooker.seat.service.SeatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/seats")
@RequiredArgsConstructor
public class SeatController {

    private final SeatService seatService;

    // Add new seat for a flight (by airline/admin)
    @PostMapping
    public ResponseEntity<SeatResponse> addSeat(@RequestBody SeatRequest request) {
        return ResponseEntity.ok(seatService.addSeat(request));
    }

    // Get all seats of a flight, Used to show full seat map
    @GetMapping("/flight/{flightId}")
    public ResponseEntity<List<SeatResponse>> getAllByFlight(@PathVariable Long flightId) {
        return ResponseEntity.ok(seatService.getSeatsByFlight(flightId));
    }

    // Get only available seats
    @GetMapping("/flight/{flightId}/available")
    public ResponseEntity<List<SeatResponse>> getAvailable(@PathVariable Long flightId) {
        return ResponseEntity.ok(seatService.getAvailableSeats(flightId));
    }

    // Get seats filtered by class (Economy/Business)
    @GetMapping("/flight/{flightId}/class/{seatClass}")
    public ResponseEntity<List<SeatResponse>> getByClass(
            @PathVariable Long flightId,
            @PathVariable String seatClass) {
        return ResponseEntity.ok(seatService.getSeatsByClass(flightId, seatClass));
    }

    // Hold seat temporarily during booking, Prevents others from selecting same seat
    @PutMapping("/flight/{flightId}/hold/{seatNumber}")
    public ResponseEntity<SeatResponse> holdSeat(
            @PathVariable Long flightId,
            @PathVariable String seatNumber) {
        return ResponseEntity.ok(seatService.holdSeat(flightId, seatNumber));
    }

    // Confirm seat after successful payment
    @PutMapping("/flight/{flightId}/confirm/{seatNumber}")
    public ResponseEntity<SeatResponse> confirmSeat(
            @PathVariable Long flightId,
            @PathVariable String seatNumber) {
        return ResponseEntity.ok(seatService.confirmSeat(flightId, seatNumber));
    }

    // Release seat if booking fails or times out and make seat available again
    @PutMapping("/flight/{flightId}/release/{seatNumber}")
    public ResponseEntity<SeatResponse> releaseSeat(
            @PathVariable Long flightId,
            @PathVariable String seatNumber) {
        return ResponseEntity.ok(seatService.releaseSeat(flightId, seatNumber));
    }

    // Get count of available seats
    @GetMapping("/flight/{flightId}/count")
    public ResponseEntity<Integer> getAvailableCount(@PathVariable Long flightId) {
        return ResponseEntity.ok(seatService.getAvailableCount(flightId));
    }
}
