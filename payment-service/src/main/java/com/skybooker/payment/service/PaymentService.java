package com.skybooker.payment.service;

import com.skybooker.payment.dto.*;

import java.util.List;

public interface PaymentService {

    /**
     * Step 1 – Create a Razorpay order.
     * Called before opening the Checkout widget on the frontend.
     */
    RazorpayOrderResponse createRazorpayOrder(RazorpayOrderRequest request);

    /**
     * Step 2 – Verify Razorpay signature after successful checkout.
     * Only on successful verification: save payment as PAID, confirm booking,
     * and send confirmation email.
     */
    PaymentResponse verifyAndConfirmPayment(PaymentVerifyRequest request);

    // ── Legacy / utility ────────────────────────────────────────────────────

    PaymentResponse getPaymentByBooking(Long bookingId);

    List<PaymentResponse> getPaymentsByUser(String userEmail);

    PaymentResponse processRefund(Long bookingId);

    List<PaymentResponse> getPaymentsByStatus(String status);
}
