package com.skybooker.airline.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity   // mapped to database
@Table(name = "airlines")   //table name in database
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Airline {

    @Id             // Primary key
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Auto-generates ID (auto-increment in DB)
    private Long id;

    private String name;           // Name of airline (IndiGo, Air India)

    @Column(unique = true)         //Ensures No duplicate IATA codes in DB
    private String iataCode;       // IATA code 6E (IndiGo), AI (Air India)

    private String icaoCode;       // IGO, AIC

    private String country;        // Country where airline is registered

    private String contactEmail;

    private String contactPhone;

    private boolean isActive;     // Admin can enable or disable airline, Used in flight search (only active airlines are shown)
}
