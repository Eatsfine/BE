package com.eatsfine.eatsfine.domain.payment.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PaymentWebhookDTO(
                @NotBlank String paymentKey,
                @NotBlank String orderId,
                @NotBlank String status,
                String eventType) {
}
