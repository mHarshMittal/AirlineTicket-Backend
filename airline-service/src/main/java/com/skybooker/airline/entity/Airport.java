package com.skybooker.airline.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "airports")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Airport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;           //  Full airport name

    @Column(unique = true)
    private String iataCode;       // Unique IATA code (3-letter) DEL, BOM, BLR

    private String icaoCode;       // ICAO code (4-letter) VIDP, VABB

    private String city;           // Delhi, Mumbai

    private String country;        // India

    private double latitude;       // Geographic coordinates (for maps/distance)

    private double longitude;

    private String timezone;       // Asia/Kolkata
}
