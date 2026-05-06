package com.skybooker.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Returned by backend after Razorpay order is created.
 * Frontend uses razorpayOrderId + keyId to open the Checkout modal.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RazorpayOrderResponse {

    private String razorpayOrderId;   // "order_XXXXXXXXXX"
    private String keyId;             // Razorpay test key_id (safe to expose)
    private long   amountInPaise;     // e.g. 59000 for ₹590
    private String currency;          // "INR"
    private Long   bookingId;
    private String userEmail;
}
