package com.skybooker.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PromoApplyResponse {
    private boolean valid;
    private String  code;
    private String  description;
    private double  discountAmount;   // actual ₹ discount
    private double  finalAmount;      // orderAmount - discountAmount
    private String  message;
}
