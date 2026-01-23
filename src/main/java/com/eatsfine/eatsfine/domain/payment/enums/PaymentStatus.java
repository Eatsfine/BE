package com.eatsfine.eatsfine.domain.payment.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PaymentStatus {
    PENDING("결제 대기"),
    COMPLETED("결제 완료"),
    FAILED("결제 실패"),
    REFUNDED("환불 완료");

    private final String description;
}
