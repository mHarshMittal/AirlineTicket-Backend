package com.skybooker.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PromoCodeResponse {
    private Long id;
    private String code;
    private String description;
    private String discountType;
    private double discountValue;
    private double maxDiscount;
    private double minOrderAmount;
    private int    usageLimit;
    private int    usedCount;
    private LocalDate expiryDate;
    private boolean active;
    private LocalDateTime createdAt;
}
