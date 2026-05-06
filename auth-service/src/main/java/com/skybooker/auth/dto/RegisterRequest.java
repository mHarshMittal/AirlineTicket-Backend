package com.skybooker.auth.dto;

import lombok.Data;

@Data
public class RegisterRequest {

    private String fullName;
    private String email;
    private String password;
    private String phone;

    private String gender;
    private String nationality;
    private String passportNumber;

    private String role;

    // Required only if registering as Airline Staff
    private String staffSecretKey;

    // Required only if registering as Admin
    private String adminSecretKey;
}