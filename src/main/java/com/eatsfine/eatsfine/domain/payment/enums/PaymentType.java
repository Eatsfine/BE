package com.eatsfine.eatsfine.domain.payment.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PaymentType {
    DEPOSIT("예약금"),
    REFUND("환불");

    private final String description;
}
