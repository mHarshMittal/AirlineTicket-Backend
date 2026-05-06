package com.skybooker.payment.controller;

import com.skybooker.payment.dto.*;
import com.skybooker.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Payment Controller — three-phase Razorpay integration:
 *
 *  1. POST /payments/create-order   → Creates Razorpay order, returns orderId + keyId
 *  2. POST /payments/verify         → Verifies HMAC signature, confirms booking, sends email
 *  3. GET  /payments/booking/{id}   → Returns payment details (used by BookingConfirm page)
 *
 * Other endpoints (user, status, refund) are unchanged.
 */
@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    // ─────────────────────────────────────────────────────────────────────────
    //  Step 1 – Create Razorpay order
    //  Frontend calls this BEFORE opening the Checkout modal.
    // ─────────────────────────────────────────────────────────────────────────
    @PostMapping("/create-order")
    public ResponseEntity<RazorpayOrderResponse> createOrder(
            @RequestBody RazorpayOrderRequest request) {
        return ResponseEntity.ok(paymentService.createRazorpayOrder(request));
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Step 2 – Verify Razorpay signature & confirm booking
    //  Frontend calls this from the Razorpay success handler.
    // ─────────────────────────────────────────────────────────────────────────
    @PostMapping("/verify")
    public ResponseEntity<PaymentResponse> verifyPayment(
            @RequestBody PaymentVerifyRequest request) {
        return ResponseEntity.ok(paymentService.verifyAndConfirmPayment(request));
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Existing endpoints — unchanged
    // ─────────────────────────────────────────────────────────────────────────

    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<PaymentResponse> getByBooking(@PathVariable Long bookingId) {
        return ResponseEntity.ok(paymentService.getPaymentByBooking(bookingId));
    }

    @GetMapping("/user/{email}")
    public ResponseEntity<List<PaymentResponse>> getByUser(@PathVariable String email) {
        return ResponseEntity.ok(paymentService.getPaymentsByUser(email));
    }

    @PostMapping("/refund/{bookingId}")
    public ResponseEntity<PaymentResponse> refund(@PathVariable Long bookingId) {
        return ResponseEntity.ok(paymentService.processRefund(bookingId));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<PaymentResponse>> getByStatus(@PathVariable String status) {
        return ResponseEntity.ok(paymentService.getPaymentsByStatus(status));
    }
}
