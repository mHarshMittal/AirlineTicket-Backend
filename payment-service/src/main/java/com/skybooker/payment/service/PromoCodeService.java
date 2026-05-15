package com.skybooker.payment.service;

import com.skybooker.payment.dto.*;
import com.skybooker.payment.entity.PromoCode;
import com.skybooker.payment.repository.PromoCodeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PromoCodeService {

    private final PromoCodeRepository promoCodeRepository;

    // ── Admin: Create a promo code ──────────────────────────────────────────
    public PromoCodeResponse createPromoCode(PromoCodeRequest request) {
        if (promoCodeRepository.findByCodeIgnoreCase(request.getCode()).isPresent()) {
            throw new RuntimeException("Promo code '" + request.getCode() + "' already exists.");
        }
        if (request.getExpiryDate() == null || request.getExpiryDate().isBefore(LocalDate.now())) {
            throw new RuntimeException("Expiry date must be today or in the future.");
        }
        if (!List.of("PERCENTAGE", "FLAT").contains(request.getDiscountType())) {
            throw new RuntimeException("discountType must be PERCENTAGE or FLAT.");
        }
        if (request.getDiscountValue() <= 0) {
            throw new RuntimeException("discountValue must be greater than 0.");
        }

        PromoCode promo = new PromoCode();
        promo.setCode(request.getCode().toUpperCase().trim());
        promo.setDescription(request.getDescription());
        promo.setDiscountType(request.getDiscountType());
        promo.setDiscountValue(request.getDiscountValue());
        promo.setMaxDiscount(request.getMaxDiscount());
        promo.setMinOrderAmount(request.getMinOrderAmount());
        promo.setUsageLimit(request.getUsageLimit());
        promo.setUsedCount(0);
        promo.setExpiryDate(request.getExpiryDate());
        promo.setActive(true);
        promo.setCreatedAt(LocalDateTime.now());

        PromoCode saved = promoCodeRepository.save(promo);
        log.info("Promo code created: {}", saved.getCode());
        return mapToResponse(saved);
    }

    // ── Admin: Get all promo codes ──────────────────────────────────────────
    public List<PromoCodeResponse> getAllPromoCodes() {
        return promoCodeRepository.findAllByOrderByCreatedAtDesc()
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    // ── Admin: Toggle active/inactive ──────────────────────────────────────
    public PromoCodeResponse togglePromoCode(Long id) {
        PromoCode promo = promoCodeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Promo code not found: " + id));
        promo.setActive(!promo.isActive());
        return mapToResponse(promoCodeRepository.save(promo));
    }

    // ── Public: Validate & calculate discount ──────────────────────────────
    public PromoApplyResponse applyPromoCode(PromoApplyRequest request) {
        PromoCode promo = promoCodeRepository.findByCodeIgnoreCase(request.getCode())
                .orElse(null);

        if (promo == null) {
            return new PromoApplyResponse(false, request.getCode(), null, 0, request.getOrderAmount(),
                    "Invalid promo code.");
        }
        if (!promo.isActive()) {
            return new PromoApplyResponse(false, promo.getCode(), null, 0, request.getOrderAmount(),
                    "This promo code is no longer active.");
        }
        if (promo.getExpiryDate().isBefore(LocalDate.now())) {
            return new PromoApplyResponse(false, promo.getCode(), null, 0, request.getOrderAmount(),
                    "This promo code has expired.");
        }
        if (promo.getUsageLimit() > 0 && promo.getUsedCount() >= promo.getUsageLimit()) {
            return new PromoApplyResponse(false, promo.getCode(), null, 0, request.getOrderAmount(),
                    "This promo code has reached its usage limit.");
        }
        if (promo.getMinOrderAmount() > 0 && request.getOrderAmount() < promo.getMinOrderAmount()) {
            return new PromoApplyResponse(false, promo.getCode(), null, 0, request.getOrderAmount(),
                    String.format("Minimum order amount ₹%.0f required for this promo.", promo.getMinOrderAmount()));
        }

        double discount;
        if ("PERCENTAGE".equals(promo.getDiscountType())) {
            discount = request.getOrderAmount() * promo.getDiscountValue() / 100.0;
            if (promo.getMaxDiscount() > 0) {
                discount = Math.min(discount, promo.getMaxDiscount());
            }
        } else {
            discount = promo.getDiscountValue();
        }
        discount = Math.min(discount, request.getOrderAmount()); // can't exceed total
        double finalAmount = Math.max(0, request.getOrderAmount() - discount);

        return new PromoApplyResponse(true, promo.getCode(), promo.getDescription(),
                Math.round(discount * 100.0) / 100.0,
                Math.round(finalAmount * 100.0) / 100.0,
                "Promo code applied successfully!");
    }

    // ── Internal: Called during payment verify to increment usage count ─────
    public void incrementUsage(String code) {
        promoCodeRepository.findByCodeIgnoreCase(code).ifPresent(p -> {
            p.setUsedCount(p.getUsedCount() + 1);
            promoCodeRepository.save(p);
        });
    }

    private PromoCodeResponse mapToResponse(PromoCode p) {
        return new PromoCodeResponse(
                p.getId(), p.getCode(), p.getDescription(),
                p.getDiscountType(), p.getDiscountValue(),
                p.getMaxDiscount(), p.getMinOrderAmount(),
                p.getUsageLimit(), p.getUsedCount(),
                p.getExpiryDate(), p.isActive(), p.getCreatedAt()
        );
    }
}
