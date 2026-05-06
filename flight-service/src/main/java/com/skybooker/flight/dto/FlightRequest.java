package com.skybooker.flight.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class FlightRequest {

    private String flightNumber;
    private String airline;
    private String source;
    private String destination;

    private LocalDate departureDate;
    private String departureTime;

    private LocalDate arrivalDate;       // arrival date (supports overnight flights)

    private String arrivalTime;     // arrival time

    private int totalSeats;     // total seat capacity of flight
    private double price;       // ticket price per seat
}