package com.skybooker.seat.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Entity
@Table(name = "seats")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Seat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // primary key

    private Long flightId; // reference to flight

    private String seatNumber; // seat number (e.g., 12A, 14C)

    private String seatClass; // ECONOMY, BUSINESS

    // 'row' is reserved keyword, so renamed column
    @Column(name = "seat_row")
    private int row; // row number

    // 'column' is reserved keyword, so renamed column
    @Column(name = "seat_column")
    private String column; // column (A, B, C, etc.)

    private boolean isWindow; // true if window seat

    private boolean isAisle; // true if aisle seat

    private boolean hasExtraLegroom; // extra leg space

    private String status; // AVAILABLE, HELD, CONFIRMED

    private double priceMultiplier; // price factor based on seat type

    private LocalDateTime holdExpiresAt; // hold expiry time
}