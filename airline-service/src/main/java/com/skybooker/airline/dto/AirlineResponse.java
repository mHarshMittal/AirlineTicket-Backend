package com.skybooker.airline.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AirlineResponse {

    private Long id;                    //Unique ID from database and other basic airline details, contact info
    private String name;
    private String iataCode;
    private String icaoCode;
    private String country;
    private String contactEmail;
    private String contactPhone;
    private boolean isActive;
    private String message;    // confirmation message
}
