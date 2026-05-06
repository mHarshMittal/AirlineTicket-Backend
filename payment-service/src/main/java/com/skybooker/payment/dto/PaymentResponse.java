package com.skybooker.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentResponse {

    private Long paymentId;
    private Long bookingId;
    private String userEmail;
    private double amount;
    private String currency;
    private String paymentMode;
    private String status;
    private String transactionId;       // razorpayPaymentId after verification
    private String razorpayOrderId;
    private String razorpayPaymentId;
    private double refundAmount;
    private LocalDateTime paidAt;
    private LocalDateTime refundedAt;
    private String message;             // Success / Failure message
}

