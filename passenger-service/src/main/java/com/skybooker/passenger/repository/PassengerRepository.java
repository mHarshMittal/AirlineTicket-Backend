package com.skybooker.passenger.repository;

import com.skybooker.passenger.entity.PassengerInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

// Repository layer -  handles DB operations for PassengerInfo
public interface PassengerRepository extends JpaRepository<PassengerInfo, Long> {

    // Get all passengers for a specific booking
    List<PassengerInfo> findByBookingId(String bookingId);

    // Find passenger using passport number
    Optional<PassengerInfo> findByPassportNumber(String passportNumber);

    // Find passenger using ticket number
    Optional<PassengerInfo> findByTicketNumber(String ticketNumber);

    // Check if a seat is already assigned to any passenger
    Optional<PassengerInfo> findBySeatId(Long seatId);

    // Count number of passengers in a booking
    int countByBookingId(String bookingId);

    // Delete all passengers for a booking (used when booking is canceled)
    void deleteByBookingId(String bookingId);
}