package com.eatsfine.eatsfine.domain.payment.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PaymentMethod {
    SIMPLE_PAYMENT("간편결제");

    private final String description;
}
