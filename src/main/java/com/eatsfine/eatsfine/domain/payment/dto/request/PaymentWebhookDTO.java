package com.eatsfine.eatsfine.domain.payment.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PaymentWebhookDTO(
                String eventType,
                @Valid @NotNull PaymentData data) {
        @JsonIgnoreProperties(ignoreUnknown = true)
        public record PaymentData(
                        @NotBlank String paymentKey,
                        @NotBlank String orderId,
                        @NotBlank String status,
                        EasyPay easyPay) {
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        public record EasyPay(
                        String provider) {
        }
}
