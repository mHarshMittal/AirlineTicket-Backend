package com.skybooker.passenger.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "passengers")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PassengerInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long passengerId;

    //linked with booking-service
    private String bookingId;

    // Basic passenger details
    private String title;           // Mr, Mrs, Ms
    private String firstName;
    private String lastName;
    private LocalDate dateOfBirth;
    private String gender;

    private String passportNumber;
    private String nationality;
    private LocalDate passportExpiry;

    // Seat details (filled during check-in)
    private Long seatId;
    private String seatNumber;

    // Unique ticket number generated during booking
    private String ticketNumber;

    // Passenger category Adult, Child, Infant (used in pricing)
    private String passengerType;

    // Record creation timestamp
    private LocalDateTime createdAt;
}
