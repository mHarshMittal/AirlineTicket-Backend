package com.skybooker.airline.service;

import com.skybooker.airline.dto.AirlineRequest;
import com.skybooker.airline.dto.AirlineResponse;
import com.skybooker.airline.dto.AirportRequest;
import com.skybooker.airline.dto.AirportResponse;

import java.util.List;

public interface AirlineService {


    /**
     * Service interface for handling business logic related to Airlines and Airports.
     * Defines operations for creating, retrieving, updating, and searching airline and airport data.
     */

    // Airline

    AirlineResponse addAirline(AirlineRequest request);

    AirlineResponse getAirlineById(Long id);

    AirlineResponse getAirlineByIata(String iataCode);

    List<AirlineResponse> getAllAirlines();

    List<AirlineResponse> getActiveAirlines();

    AirlineResponse updateAirline(Long id, AirlineRequest request);

    AirlineResponse toggleAirlineStatus(Long id);

    // Airport
    AirportResponse addAirport(AirportRequest request);

    AirportResponse getAirportById(Long id);

    AirportResponse getAirportByIata(String iataCode);

    List<AirportResponse> getAllAirports();

    List<AirportResponse> searchAirports(String keyword);

    AirportResponse updateAirport(Long id, AirportRequest request);
}
