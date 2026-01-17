package com.eatsfine.eatsfine.domain.payment.dto.request;

import com.eatsfine.eatsfine.domain.payment.enums.PaymentProvider;
import jakarta.validation.constraints.NotNull;

public class PaymentRequestDTO {

    public record RequestPaymentDTO(
            @NotNull Long bookingId,
            @NotNull PaymentProvider provider,
            @NotNull String successUrl,
            @NotNull String failUrl) {
    }
}
