package com.eatsfine.eatsfine.domain.payment.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PaymentProvider {
    KAKAOPAY("카카오페이"),
    TOSS("토스");

    private final String description;
}
