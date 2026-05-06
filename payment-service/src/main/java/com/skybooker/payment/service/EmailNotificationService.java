package com.skybooker.payment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Sends booking-confirmation emails after successful Razorpay payment verification.
 * Uses JavaMailSender (Spring Boot Starter Mail → Gmail SMTP).
 * Marked @Async so it never blocks the payment-verification HTTP response.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailNotificationService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    /**
     * All the booking details needed to compose the confirmation email.
     * Collected by PaymentServiceImpl from passenger-service + flight-service.
     */
    public record BookingEmailDetails(
            String toEmail,
            String userName,
            Long   bookingId,
            String pnrNumber,
            String flightNumber,
            String airline,
            String source,
            String destination,
            String departureDate,
            String departureTime,
            String arrivalTime,
            List<PassengerDetail> passengers,
            double amountPaid,
            String transactionId,
            String paymentMode,
            LocalDateTime paidAt
    ) {}

    public record PassengerDetail(
            String name,
            String seatNumber,
            String ticketNumber,
            String passengerType
    ) {}

    /**
     * Sends the HTML booking confirmation email asynchronously.
     * If email fails (e.g. SMTP misconfigured) it only logs an error —
     * the payment is already confirmed so we must not throw.
     */
    @Async
    public void sendBookingConfirmation(BookingEmailDetails details) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(details.toEmail());
            helper.setSubject("✈ Booking Confirmed! PNR: " + details.pnrNumber()
                    + " | " + details.source() + " → " + details.destination());
            helper.setText(buildHtmlBody(details), true);   // true = HTML

            mailSender.send(message);
            log.info("Booking confirmation email sent to {} for booking #{}", details.toEmail(), details.bookingId());

        } catch (Exception e) {
            // Log but do NOT rethrow – email failure must not roll back payment confirmation
            log.error("Failed to send booking confirmation email to {}: {}", details.toEmail(), e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  HTML email template
    // ─────────────────────────────────────────────────────────────────────────
    private String buildHtmlBody(BookingEmailDetails d) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");
        String paidAtFormatted = d.paidAt() != null ? d.paidAt().format(fmt) : "N/A";

        StringBuilder passengerRows = new StringBuilder();
        int idx = 1;
        for (PassengerDetail p : d.passengers()) {
            passengerRows.append("""
                    <tr style="background:%s;">
                      <td style="padding:10px 14px;border-bottom:1px solid #e5e7eb;">%d</td>
                      <td style="padding:10px 14px;border-bottom:1px solid #e5e7eb;font-weight:600;">%s</td>
                      <td style="padding:10px 14px;border-bottom:1px solid #e5e7eb;">%s</td>
                      <td style="padding:10px 14px;border-bottom:1px solid #e5e7eb;">%s</td>
                      <td style="padding:10px 14px;border-bottom:1px solid #e5e7eb;font-family:monospace;">%s</td>
                    </tr>
                    """.formatted(
                    idx % 2 == 0 ? "#f9fafb" : "#ffffff",
                    idx++,
                    escHtml(p.name()),
                    p.passengerType(),
                    p.seatNumber() != null ? p.seatNumber() : "To be assigned",
                    escHtml(p.ticketNumber())
            ));
        }

        return """
            <!DOCTYPE html>
            <html lang="en">
            <head><meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1"></head>
            <body style="margin:0;padding:0;background:#f3f4f6;font-family:'Segoe UI',Arial,sans-serif;">
              <table width="100%%" cellpadding="0" cellspacing="0" style="background:#f3f4f6;padding:32px 0;">
                <tr><td align="center">
                  <table width="620" cellpadding="0" cellspacing="0" style="background:#ffffff;border-radius:12px;overflow:hidden;box-shadow:0 4px 24px rgba(0,0,0,.08);">
                    
                    <!-- Header -->
                    <tr><td style="background:linear-gradient(135deg,#1d4ed8 0%%,#2563eb 60%%,#0ea5e9 100%%);padding:32px 40px;text-align:center;">
                      <div style="font-size:36px;">✈</div>
                      <h1 style="color:#fff;margin:8px 0 4px;font-size:26px;font-weight:700;letter-spacing:-0.5px;">SkyBooker</h1>
                      <p style="color:#bfdbfe;margin:0;font-size:14px;">Your ticket to the skies</p>
                    </td></tr>

                    <!-- Success Banner -->
                    <tr><td style="background:#ecfdf5;padding:20px 40px;text-align:center;border-bottom:2px solid #6ee7b7;">
                      <span style="font-size:28px;">✅</span>
                      <h2 style="color:#065f46;margin:8px 0 4px;font-size:20px;">Booking Confirmed!</h2>
                      <p style="color:#047857;margin:0;font-size:14px;">Your payment was successful and your flight is booked.</p>
                    </td></tr>

                    <!-- Body -->
                    <tr><td style="padding:32px 40px;">

                      <p style="color:#374151;font-size:15px;margin:0 0 24px;">
                        Dear <strong>%s</strong>, your booking is confirmed. Below are your flight details.
                      </p>

                      <!-- PNR Highlight -->
                      <div style="background:#eff6ff;border:2px solid #bfdbfe;border-radius:10px;padding:18px 24px;text-align:center;margin-bottom:24px;">
                        <p style="margin:0 0 4px;color:#6b7280;font-size:12px;text-transform:uppercase;letter-spacing:1px;">PNR Number</p>
                        <p style="margin:0;color:#1d4ed8;font-size:28px;font-weight:800;letter-spacing:4px;font-family:monospace;">%s</p>
                      </div>

                      <!-- Flight Details -->
                      <h3 style="color:#111827;font-size:16px;margin:0 0 14px;padding-bottom:8px;border-bottom:2px solid #e5e7eb;">
                        🛫 Flight Details
                      </h3>
                      <table width="100%%" cellpadding="0" cellspacing="0" style="margin-bottom:28px;">
                        %s
                        %s
                        %s
                        %s
                        %s
                      </table>

                      <!-- Passengers -->
                      <h3 style="color:#111827;font-size:16px;margin:0 0 14px;padding-bottom:8px;border-bottom:2px solid #e5e7eb;">
                        👤 Passenger Details
                      </h3>
                      <table width="100%%" cellpadding="0" cellspacing="0" style="border-collapse:collapse;border-radius:8px;overflow:hidden;border:1px solid #e5e7eb;margin-bottom:28px;">
                        <thead>
                          <tr style="background:#1d4ed8;">
                            <th style="padding:10px 14px;color:#fff;text-align:left;font-size:13px;">#</th>
                            <th style="padding:10px 14px;color:#fff;text-align:left;font-size:13px;">Name</th>
                            <th style="padding:10px 14px;color:#fff;text-align:left;font-size:13px;">Type</th>
                            <th style="padding:10px 14px;color:#fff;text-align:left;font-size:13px;">Seat</th>
                            <th style="padding:10px 14px;color:#fff;text-align:left;font-size:13px;">Ticket No.</th>
                          </tr>
                        </thead>
                        <tbody>%s</tbody>
                      </table>

                      <!-- Payment Details -->
                      <h3 style="color:#111827;font-size:16px;margin:0 0 14px;padding-bottom:8px;border-bottom:2px solid #e5e7eb;">
                        💳 Payment Details
                      </h3>
                      <table width="100%%" cellpadding="0" cellspacing="0" style="margin-bottom:28px;">
                        %s
                        %s
                        %s
                        %s
                      </table>

                      <!-- Status Badge -->
                      <div style="text-align:center;margin:24px 0;">
                        <span style="background:#dcfce7;color:#166534;padding:8px 24px;border-radius:999px;font-size:14px;font-weight:700;border:1px solid #86efac;">
                          ✓ BOOKING STATUS: CONFIRMED
                        </span>
                      </div>

                      <!-- Info Note -->
                      <div style="background:#fffbeb;border-left:4px solid #f59e0b;padding:14px 18px;border-radius:0 8px 8px 0;margin-bottom:24px;">
                        <p style="margin:0;color:#92400e;font-size:13px;">
                          ⚠ Please arrive at the airport at least 2 hours before departure. 
                          Carry a valid government-issued photo ID along with this e-ticket.
                        </p>
                      </div>

                    </td></tr>

                    <!-- Footer -->
                    <tr><td style="background:#f9fafb;padding:20px 40px;text-align:center;border-top:1px solid #e5e7eb;">
                      <p style="color:#9ca3af;font-size:12px;margin:0;">
                        This is an automated email from SkyBooker. Please do not reply to this email.<br>
                        © 2025 SkyBooker. All rights reserved. | Booking ID: #%d
                      </p>
                    </td></tr>

                  </table>
                </td></tr>
              </table>
            </body>
            </html>
            """.formatted(
                escHtml(d.userName()),
                escHtml(d.pnrNumber()),
                detailRow("Flight Number",  d.flightNumber()),
                detailRow("Airline",        d.airline()),
                detailRow("Route",          d.source() + " → " + d.destination()),
                detailRow("Departure",      d.departureDate() + " at " + d.departureTime()),
                detailRow("Arrival",        d.arrivalTime()),
                passengerRows.toString(),
                detailRow("Amount Paid",    "₹" + String.format("%.2f", d.amountPaid())),
                detailRow("Payment Mode",   d.paymentMode()),
                detailRow("Transaction ID", d.transactionId()),
                detailRow("Paid At",        paidAtFormatted),
                d.bookingId()
        );
    }

    private String detailRow(String label, String value) {
        return """
            <tr>
              <td style="padding:7px 0;color:#6b7280;font-size:14px;width:40%%;">%s</td>
              <td style="padding:7px 0;color:#111827;font-size:14px;font-weight:600;">%s</td>
            </tr>
            """.formatted(escHtml(label), escHtml(value != null ? value : "N/A"));
    }

    private String escHtml(String s) {
        if (s == null) return "";
        return s.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;").replace("\"","&quot;");
    }
}
