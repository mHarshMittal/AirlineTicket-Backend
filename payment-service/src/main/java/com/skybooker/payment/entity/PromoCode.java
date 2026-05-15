package com.skybooker.payment.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "promo_codes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PromoCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String code;            // e.g. "SAVE20"

    private String description;     // e.g. "20% off on all flights"

    // PERCENTAGE or FLAT
    @Column(nullable = false)
    private String discountType;    // "PERCENTAGE" | "FLAT"

    @Column(nullable = false)
    private double discountValue;   // e.g. 20 (for 20%) or 500 (for ₹500 off)

    private double maxDiscount;     // cap for percentage discounts (0 = no cap)
    private double minOrderAmount;  // minimum cart value to apply promo

    private int usageLimit;         // max times this code can be used (0 = unlimited)
    private int usedCount;          // how many times it has been used

    @Column(nullable = false)
    private LocalDate expiryDate;

    private boolean active;

    private LocalDateTime createdAt;
}
