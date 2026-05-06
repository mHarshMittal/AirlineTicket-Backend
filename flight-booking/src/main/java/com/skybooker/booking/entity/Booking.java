package com.skybooker.booking.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long   flightId;
    private String userEmail;

    private int    seatsBooked;
    private double totalPrice;

    /**
     * Flow:
     *  PENDING   – seats reduced, booking saved, waiting for payment
     *  CONFIRMED – payment verified by payment-service
     *  CANCELLED – user cancelled or payment failed
     */
    private String status;

    /** Passenger Name Record – used in email and boarding pass. */
    private String pnr;

    private LocalDateTime bookingTime;
}
