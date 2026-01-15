package com.eatsfine.eatsfine.domain.payment.dto.response;

import com.eatsfine.eatsfine.domain.payment.enums.PaymentMethod;
import com.eatsfine.eatsfine.domain.payment.enums.PaymentStatus;

import java.time.LocalDateTime;

public class PaymentResponseDTO {

    public record PaymentRequestResultDTO(
            Long paymentId,
            Long bookingId,
            PaymentMethod paymentMethod,
            String tid,
            Integer amount,
            PaymentStatus paymentStatus,
            String nextRedirectUrl,
            LocalDateTime requestedAt) {
    }
}
