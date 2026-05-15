package com.skybooker.payment.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class PromoCodeRequest {
    private String code;
    private String description;
    private String discountType;   // "PERCENTAGE" | "FLAT"
    private double discountValue;
    private double maxDiscount;    // 0 = no cap
    private double minOrderAmount; // 0 = no minimum
    private int    usageLimit;     // 0 = unlimited
    private LocalDate expiryDate;
}
