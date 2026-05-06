package com.skybooker.payment.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long bookingId;       // link with booking-service
    private String userEmail;

    private double amount;      // total amount paid (including taxes)
    private String currency;    // INR

    // CARD, UPI, NETBANKING, WALLET
    private String paymentMode;

    // PENDING, PAID, FAILED, REFUNDED
    private String status;

    // Razorpay identifiers
    private String razorpayOrderId;    // "order_XXXXXXXXXX"  – created before checkout
    private String razorpayPaymentId;  // "pay_XXXXXXXXXX"   – returned after payment
    private String razorpaySignature;  // HMAC-SHA256 signature verified server-side

    // Fallback / legacy unique transaction reference
    private String transactionId;

    // refund related fields
    private double refundAmount;
    private LocalDateTime refundedAt;

    private LocalDateTime createdAt;
    private LocalDateTime paidAt;
}

