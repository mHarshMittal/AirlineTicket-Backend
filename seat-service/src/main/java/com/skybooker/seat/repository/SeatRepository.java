package com.skybooker.seat.repository;

import com.skybooker.seat.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SeatRepository extends JpaRepository<Seat, Long> {

    // Get all seats of a flight
    List<Seat> findByFlightId(Long flightId);

    // Get seats by flight and status ( AVAILABLE)
    List<Seat> findByFlightIdAndStatus(Long flightId, String status);

    // Get seats by class (ECONOMY, BUSINESS)
    List<Seat> findByFlightIdAndSeatClass(Long flightId, String seatClass);

    // Find a specific seat using seat number (e.g., 14B)
    Optional<Seat> findByFlightIdAndSeatNumber(Long flightId, String seatNumber);

    // Find seats with expired hold (used by scheduler)
    List<Seat> findByStatusAndHoldExpiresAtBefore(String status, LocalDateTime time);

    // Count available seats for a flight
    int countByFlightIdAndStatus(Long flightId, String status);
}
