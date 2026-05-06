package com.skybooker.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request body sent by frontend to create a Razorpay order.
 * The backend creates the order via Razorpay API and returns the orderId
 * which the frontend uses to open the Razorpay Checkout widget.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RazorpayOrderRequest {

    private Long bookingId;
    private String userEmail;
    private double amount;          // base amount (without taxes) – taxes added server-side
    private String currency;        // "INR"
}
