package com.skybooker.seat.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SeatRequest {

    private Long flightId;
    private String seatNumber;   // seat number
    private String seatClass;    // Economy, Business
    private int row;             // Row number of seat
    private String column;       // A, B, C, D
    private boolean isWindow;
    private boolean isAisle;
    private boolean hasExtraLegroom;
    private double priceMultiplier;  // price factor 1.0 default
}
