package com.skybooker.airline.controller;

import com.skybooker.airline.dto.AirlineRequest;
import com.skybooker.airline.dto.AirlineResponse;
import com.skybooker.airline.dto.AirportRequest;
import com.skybooker.airline.dto.AirportResponse;
import com.skybooker.airline.service.AirlineService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class AirlineController {

    private final AirlineService airlineService;

    // Add Airline (Admin)
    @PostMapping("/airlines")
    public ResponseEntity<AirlineResponse> addAirline(@RequestBody AirlineRequest request) {
        return ResponseEntity.ok(airlineService.addAirline(request));
    }

    //Get Airline by ID
    @GetMapping("/airlines/{id}")
    public ResponseEntity<AirlineResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(airlineService.getAirlineById(id));
    }

    // International Air Transport Association code (3 digits code to uniquely identify like BOM, DEL, HYd...

    @GetMapping("/airlines/iata/{iataCode}")
    public ResponseEntity<AirlineResponse> getByIata(@PathVariable String iataCode) {
        return ResponseEntity.ok(airlineService.getAirlineByIata(iataCode));
    }

    // Get All Airlines
    @GetMapping("/airlines")
    public ResponseEntity<List<AirlineResponse>> getAll() {
        return ResponseEntity.ok(airlineService.getAllAirlines());
    }

    //Get Active Airlines
    @GetMapping("/airlines/active")
    public ResponseEntity<List<AirlineResponse>> getActive() {
        return ResponseEntity.ok(airlineService.getActiveAirlines());
    }

    // Update Airline
    @PutMapping("/airlines/{id}")
    public ResponseEntity<AirlineResponse> update(
            @PathVariable Long id,
            @RequestBody AirlineRequest request) {
        return ResponseEntity.ok(airlineService.updateAirline(id, request));
    }

    // aActivate / deactivate airline
    @PutMapping("/airlines/{id}/toggle-status")
    public ResponseEntity<AirlineResponse> toggleStatus(@PathVariable Long id) {
        return ResponseEntity.ok(airlineService.toggleAirlineStatus(id));
    }

            // ==================== AIRPORT ENDPOINTS ====================

    // Add Airport (Admin will add new Airport
    @PostMapping("/airports")
    public ResponseEntity<AirportResponse> addAirport(@RequestBody AirportRequest request) {
        return ResponseEntity.ok(airlineService.addAirport(request));
    }

    // Get Airport by ID
    @GetMapping("/airports/{id}")
    public ResponseEntity<AirportResponse> getAirportById(@PathVariable Long id) {
        return ResponseEntity.ok(airlineService.getAirportById(id));
    }

    // Get by IATA( DEL, BOM, HYD..)
    @GetMapping("/airports/iata/{iataCode}")
    public ResponseEntity<AirportResponse> getAirportByIata(@PathVariable String iataCode) {
        return ResponseEntity.ok(airlineService.getAirportByIata(iataCode));
    }

    // Get All Airports
    @GetMapping("/airports")
    public ResponseEntity<List<AirportResponse>> getAllAirports() {
        return ResponseEntity.ok(airlineService.getAllAirports());
    }

    // Search Airport for filling the flight Booking Form
    @GetMapping("/airports/search")
    public ResponseEntity<List<AirportResponse>> searchAirports(@RequestParam String keyword) {
        return ResponseEntity.ok(airlineService.searchAirports(keyword));
    }

    // Update Airport
    @PutMapping("/airports/{id}")
    public ResponseEntity<AirportResponse> updateAirport(
            @PathVariable Long id,
            @RequestBody AirportRequest request) {
        return ResponseEntity.ok(airlineService.updateAirport(id, request));
    }
}
