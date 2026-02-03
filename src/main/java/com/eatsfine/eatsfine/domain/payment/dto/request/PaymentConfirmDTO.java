package com.eatsfine.eatsfine.domain.payment.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record PaymentConfirmDTO(
        @NotNull String paymentKey,
        @NotNull String orderId,
        @NotNull BigDecimal amount) {
}
