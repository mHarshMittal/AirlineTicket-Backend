package com.skybooker.payment.controller;

import com.skybooker.payment.dto.*;
import com.skybooker.payment.service.PromoCodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/payments/promo")
@RequiredArgsConstructor
public class PromoCodeController {

    private final PromoCodeService promoCodeService;

    // ── Admin only: Create promo code ─────────────────────────────────────
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PromoCodeResponse> create(@RequestBody PromoCodeRequest request) {
        return ResponseEntity.ok(promoCodeService.createPromoCode(request));
    }

    // ── Admin only: List all promo codes ─────────────────────────────────
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<PromoCodeResponse>> getAll() {
        return ResponseEntity.ok(promoCodeService.getAllPromoCodes());
    }

    // ── Admin only: Toggle active/inactive ───────────────────────────────
    @PutMapping("/{id}/toggle")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PromoCodeResponse> toggle(@PathVariable Long id) {
        return ResponseEntity.ok(promoCodeService.togglePromoCode(id));
    }

    // ── Passenger: Validate & calculate discount (no side-effects) ───────
    @PostMapping("/apply")
    @PreAuthorize("hasRole('PASSENGER')")
    public ResponseEntity<PromoApplyResponse> apply(@RequestBody PromoApplyRequest request) {
        return ResponseEntity.ok(promoCodeService.applyPromoCode(request));
    }
}
