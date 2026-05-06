package com.skybooker.airline.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AirlineRequest {

    private String name;            // Airline name : Indigo
    private String iataCode;        // Airline code like 6E or AI
    private String icaoCode;        // 3 letter international code  IGO - IndiGo , AIC - Air India
    private String country;         // India
    private String contactEmail;    // Support Mail id
    private String contactPhone;    // Contact no
}
