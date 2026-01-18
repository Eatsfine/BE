package com.eatsfine.eatsfine.domain.payment.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record PaymentConfirmDTO(
        @NotNull String paymentKey,
        @NotNull String orderId,
        @NotNull Integer amount) {
}
