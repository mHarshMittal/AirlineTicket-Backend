package com.skybooker.payment.service;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.skybooker.payment.dto.*;
import com.skybooker.payment.entity.Payment;
import com.skybooker.payment.repository.PaymentRepository;
import com.skybooker.payment.service.EmailNotificationService.BookingEmailDetails;
import com.skybooker.payment.service.EmailNotificationService.PassengerDetail;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository       paymentRepository;
    private final EmailNotificationService emailService;
    private final RestTemplate            restTemplate;
    private final PromoCodeService        promoCodeService;

    @Value("${razorpay.key.id}")
    private String razorpayKeyId;

    @Value("${razorpay.key.secret}")
    private String razorpayKeySecret;

    @Value("${service.booking.url}")
    private String bookingServiceUrl;

    @Value("${service.passenger.url}")
    private String passengerServiceUrl;

    @Value("${service.flight.url}")
    private String flightServiceUrl;

    @Value("${jwt.secret}")
    private String jwtSecret;

    // ─────────────────────────────────────────────────────────────────────────
    //  STEP 1 – Create Razorpay Order
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public RazorpayOrderResponse createRazorpayOrder(RazorpayOrderRequest request) {

        // Guard: don't create a second order if payment already PAID
        paymentRepository.findByBookingId(request.getBookingId()).ifPresent(p -> {
            if ("PAID".equals(p.getStatus())) {
                throw new RuntimeException("Payment already completed for booking: " + request.getBookingId());
            }
        });

        long amountInPaise = Math.round(request.getAmount() * 100);
        String currency    = request.getCurrency() != null ? request.getCurrency() : "INR";

        JSONObject orderReq = new JSONObject();
        orderReq.put("amount",          amountInPaise);
        orderReq.put("currency",        currency);
        orderReq.put("receipt",         "booking_" + request.getBookingId());
        orderReq.put("payment_capture", 1);

        try {
            RazorpayClient razorpay = new RazorpayClient(razorpayKeyId, razorpayKeySecret);
            Order          order    = razorpay.orders.create(orderReq);
            String razorpayOrderId  = order.get("id");
            log.info("Razorpay order created: {} for booking #{}", razorpayOrderId, request.getBookingId());

            // Persist PENDING payment record
            Payment payment = new Payment();
            payment.setBookingId(request.getBookingId());
            payment.setUserEmail(request.getUserEmail());
            payment.setAmount(request.getAmount());
            payment.setCurrency(currency);
            payment.setStatus("PENDING");
            payment.setRazorpayOrderId(razorpayOrderId);
            payment.setCreatedAt(LocalDateTime.now());
            paymentRepository.save(payment);

            return new RazorpayOrderResponse(
                    razorpayOrderId, razorpayKeyId,
                    amountInPaise, currency,
                    request.getBookingId(), request.getUserEmail());

        } catch (RazorpayException e) {
            log.error("Razorpay order creation failed: {}", e.getMessage());
            throw new RuntimeException("Failed to create Razorpay order: " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  STEP 2 – Verify Signature → Confirm Booking → Send Email
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public PaymentResponse verifyAndConfirmPayment(PaymentVerifyRequest request) {

        // 1. Cryptographic signature verification
        verifySignature(request.getRazorpayOrderId(),
                        request.getRazorpayPaymentId(),
                        request.getRazorpaySignature());

        // 2. Load the pending payment record
        Payment payment = paymentRepository.findByRazorpayOrderId(request.getRazorpayOrderId())
                .orElseThrow(() -> new RuntimeException(
                        "No pending payment for Razorpay order: " + request.getRazorpayOrderId()));

        if ("PAID".equals(payment.getStatus())) {
            throw new RuntimeException("Payment already verified: " + request.getRazorpayOrderId());
        }

        // 3. Mark PAID
        payment.setRazorpayPaymentId(request.getRazorpayPaymentId());
        payment.setRazorpaySignature(request.getRazorpaySignature());
        payment.setTransactionId(request.getRazorpayPaymentId());
        payment.setPaymentMode(request.getPaymentMode());
        payment.setStatus("PAID");
        payment.setPaidAt(LocalDateTime.now());
        Payment saved = paymentRepository.save(payment);
        log.info("Payment PAID – Razorpay paymentId: {}", request.getRazorpayPaymentId());

        // 4. Confirm booking (PENDING → CONFIRMED) via booking-service
        confirmBooking(payment.getBookingId());

        // 4b. Increment promo code usage if one was applied
        if (request.getPromoCode() != null && !request.getPromoCode().isBlank()) {
            promoCodeService.incrementUsage(request.getPromoCode());
        }

        // 5. Send email asynchronously (errors are swallowed to keep response fast)
        try { sendConfirmationEmail(saved); }
        catch (Exception e) {
            log.error("Email dispatch error for booking #{}: {}", saved.getBookingId(), e.getMessage());
        }

        return mapToResponse(saved, "Payment verified successfully. Booking confirmed!");
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Existing helpers
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public PaymentResponse getPaymentByBooking(Long bookingId) {
        Payment p = paymentRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new RuntimeException("No payment found for booking: " + bookingId));
        return mapToResponse(p, "Success");
    }

    @Override
    public List<PaymentResponse> getPaymentsByUser(String userEmail) {
        return paymentRepository.findByUserEmail(userEmail).stream()
                .map(p -> mapToResponse(p, "Success"))
                .collect(Collectors.toList());
    }

    @Override
    public PaymentResponse processRefund(Long bookingId) {
        Payment p = paymentRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new RuntimeException("No payment found for booking: " + bookingId));
        if (!"PAID".equals(p.getStatus()))
            throw new RuntimeException("Refund not possible – status: " + p.getStatus());
        p.setStatus("REFUNDED");
        p.setRefundAmount(p.getAmount());
        p.setRefundedAt(LocalDateTime.now());
        return mapToResponse(paymentRepository.save(p),
                "Refund processed. Amount credited in 5-7 working days.");
    }

    @Override
    public List<PaymentResponse> getPaymentsByStatus(String status) {
        return paymentRepository.findByStatus(status).stream()
                .map(p -> mapToResponse(p, "Success"))
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Private utilities
    // ─────────────────────────────────────────────────────────────────────────

    private void verifySignature(String orderId, String paymentId, String receivedSig) {
        try {
            String payload = orderId + "|" + paymentId;
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(razorpayKeySecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] hash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            StringBuilder computed = new StringBuilder();
            for (byte b : hash) computed.append(String.format("%02x", b));

            if (!computed.toString().equals(receivedSig)) {
                throw new RuntimeException("Razorpay signature verification FAILED. Possible tampering.");
            }
            log.info("Signature verified OK for order: {}", orderId);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Signature verification error: " + e.getMessage());
        }
    }

    private void confirmBooking(Long bookingId) {
        try {
            HttpHeaders h = new HttpHeaders();
            h.set("Authorization", "Bearer " + buildInternalJwt());
            restTemplate.exchange(
                    bookingServiceUrl + "/bookings/" + bookingId + "/confirm",
                    HttpMethod.PUT, new HttpEntity<>(h), String.class);
            log.info("Booking #{} confirmed via booking-service", bookingId);
        } catch (Exception e) {
            log.error("Could not confirm booking #{}: {}", bookingId, e.getMessage());
        }
    }

    private void sendConfirmationEmail(Payment payment) {
        HttpHeaders h = new HttpHeaders();
        h.set("Authorization", "Bearer " + buildInternalJwt());
        HttpEntity<Void> entity = new HttpEntity<>(h);

        // Passengers
        List<Map<String, Object>> passengersRaw = new ArrayList<>();
        try {
            ResponseEntity<List<Map<String, Object>>> r = restTemplate.exchange(
                    passengerServiceUrl + "/passengers/booking/" + payment.getBookingId(),
                    HttpMethod.GET, entity, new ParameterizedTypeReference<>() {});
            if (r.getBody() != null) passengersRaw = r.getBody();
        } catch (Exception e) { log.warn("Passenger fetch failed for email: {}", e.getMessage()); }

        // Booking
        Map<String, Object> bookingData = new HashMap<>();
        try {
            ResponseEntity<Map<String, Object>> r = restTemplate.exchange(
                    bookingServiceUrl + "/bookings/" + payment.getBookingId(),
                    HttpMethod.GET, entity, new ParameterizedTypeReference<>() {});
            if (r.getBody() != null) bookingData = r.getBody();
        } catch (Exception e) { log.warn("Booking fetch failed for email: {}", e.getMessage()); }

        // Flight
        Map<String, Object> flightData = new HashMap<>();
        Object flightIdObj = bookingData.get("flightId");
        if (flightIdObj != null) {
            try {
                ResponseEntity<Map<String, Object>> r = restTemplate.exchange(
                        flightServiceUrl + "/flights/" + flightIdObj,
                        HttpMethod.GET, entity, new ParameterizedTypeReference<>() {});
                if (r.getBody() != null) flightData = r.getBody();
            } catch (Exception e) { log.warn("Flight fetch failed for email: {}", e.getMessage()); }
        }

        List<PassengerDetail> passengerDetails = passengersRaw.stream().map(p ->
                new PassengerDetail(
                        safeStr(p.get("title")) + " " + safeStr(p.get("firstName")) + " " + safeStr(p.get("lastName")),
                        safeStr(p.get("seatNumber")),
                        safeStr(p.get("ticketNumber")),
                        safeStr(p.get("passengerType"))
                )).collect(Collectors.toList());

        String pnr = safeStr(bookingData.get("pnr"));
        if (pnr.isBlank()) pnr = "SKY" + String.format("%07d", payment.getBookingId());

        String userName = passengerDetails.isEmpty()
                ? payment.getUserEmail()
                : passengerDetails.get(0).name();

        emailService.sendBookingConfirmation(new BookingEmailDetails(
                payment.getUserEmail(), userName,
                payment.getBookingId(), pnr,
                safeStr(flightData.get("flightNumber")),
                safeStr(flightData.get("airline")),
                safeStr(flightData.get("source")),
                safeStr(flightData.get("destination")),
                safeStr(flightData.get("departureDate")),
                safeStr(flightData.get("departureTime")),
                safeStr(flightData.get("arrivalTime")),
                passengerDetails,
                payment.getAmount(),
                payment.getTransactionId(),
                payment.getPaymentMode() != null ? payment.getPaymentMode() : "Online",
                payment.getPaidAt()
        ));
    }

    private String buildInternalJwt() {
        try {
            javax.crypto.SecretKey key = io.jsonwebtoken.security.Keys
                    .hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
            return io.jsonwebtoken.Jwts.builder()
                    .setSubject("payment-service-internal")
                    .claim("role", "AIRLINE_STAFF")
                    .setIssuedAt(new Date())
                    .setExpiration(new Date(System.currentTimeMillis() + 5 * 60 * 1000))
                    .signWith(key)
                    .compact();
        } catch (Exception e) {
            log.warn("Internal JWT build error: {}", e.getMessage());
            return "";
        }
    }

    private String safeStr(Object o) { return o != null ? o.toString() : ""; }

    private PaymentResponse mapToResponse(Payment p, String message) {
        PaymentResponse r = new PaymentResponse();
        r.setPaymentId(p.getId());
        r.setBookingId(p.getBookingId());
        r.setUserEmail(p.getUserEmail());
        r.setAmount(p.getAmount());
        r.setCurrency(p.getCurrency());
        r.setPaymentMode(p.getPaymentMode());
        r.setStatus(p.getStatus());
        r.setTransactionId(p.getTransactionId());
        r.setRazorpayOrderId(p.getRazorpayOrderId());
        r.setRazorpayPaymentId(p.getRazorpayPaymentId());
        r.setRefundAmount(p.getRefundAmount());
        r.setPaidAt(p.getPaidAt());
        r.setRefundedAt(p.getRefundedAt());
        r.setMessage(message);
        return r;
    }
}
