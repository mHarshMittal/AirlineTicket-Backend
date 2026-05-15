package com.skybooker.payment.dto;

import lombok.Data;

@Data
public class PromoApplyRequest {
    private String code;
    private double orderAmount;  // amount BEFORE discount
}
