package com.skybooker.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Sent by frontend after user completes payment in Razorpay Checkout.
 * Contains the three identifiers Razorpay sends in the success handler.
 * Backend verifies HMAC-SHA256 signature before confirming the booking.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentVerifyRequest {

    // From Razorpay Checkout success handler
    private String razorpayOrderId;       // "order_XXXXXXXXXX"
    private String razorpayPaymentId;     // "pay_XXXXXXXXXX"
    private String razorpaySignature;     // HMAC-SHA256 signature

    // Our own fields so we can confirm the right booking
    private Long   bookingId;
    private String userEmail;
    private double amount;
    private String paymentMode;           // CARD, UPI, NETBANKING, WALLET
}
