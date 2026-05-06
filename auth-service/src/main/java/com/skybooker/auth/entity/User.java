package com.skybooker.auth.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fullName;

    @Email
    @Column(unique = true, nullable = false)
    private String email;

    private String password;

    @Column(nullable = false)
    private String phone;

    // Optional profile information

    private String gender;
    private LocalDate dateOfBirth;
    private String nationality;
    private String passportNumber;
    private LocalDate passportExpiry;
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String state;
    private String country;
    private String pincode;

    @Column(nullable = false)
    private String role;

    @Column(nullable = false)
    private boolean isActive;

    @Column(nullable = false)
    private boolean isVerified;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
