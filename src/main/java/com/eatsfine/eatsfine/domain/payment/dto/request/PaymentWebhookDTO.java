package com.eatsfine.eatsfine.domain.payment.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PaymentWebhookDTO(
        String paymentKey,
        String orderId,
        String status,
        String eventType) {
}
