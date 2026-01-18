package com.eatsfine.eatsfine.domain.payment.dto.request;

import jakarta.validation.constraints.NotNull;

public class PaymentRequestDTO {

    public record RequestPaymentDTO(
            @NotNull Long bookingId) {
    }
}
