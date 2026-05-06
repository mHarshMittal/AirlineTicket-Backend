package com.skybooker.flight.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class FlightResponse {    // Used to send flight data from backend to frontend (API response)


    // Unique ID of the flight (primary key from DB)
    private Long id;

    // Flight number
    private String flightNumber;

    // Airline name (IndiGo, Air India)
    private String airline;

    // Source airport/city code (DEL)
    private String source;

    // Destination airport/city code ( BOM)
    private String destination;

    // Date of departure (only date, no time)
    private LocalDate departureDate;

    // Time of departure (stored as String like "10:30 AM")
    private String departureTime;

    // Date of arrival
    private LocalDate arrivalDate;

    // Time of arrival
    private String arrivalTime;

    // Total available seats left for booking
    private int availableSeats;

    // Ticket price for this flight
    private double price;
}