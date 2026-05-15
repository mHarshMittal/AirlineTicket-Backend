package com.skybooker.payment.repository;

import com.skybooker.payment.entity.PromoCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PromoCodeRepository extends JpaRepository<PromoCode, Long> {
    Optional<PromoCode> findByCodeIgnoreCase(String code);
    List<PromoCode> findAllByOrderByCreatedAtDesc();
}
