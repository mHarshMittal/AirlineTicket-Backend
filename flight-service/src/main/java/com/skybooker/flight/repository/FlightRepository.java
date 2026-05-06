package com.skybooker.flight.repository;

import com.skybooker.flight.entity.Flight;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface FlightRepository extends JpaRepository<Flight, Long> {

    // This will fetch flights based on:
    // source + destination + departure date
    List<Flight> findBySourceAndDestinationAndDepartureDate(
            String source,
            String destination,
            LocalDate departureDate
    );
}