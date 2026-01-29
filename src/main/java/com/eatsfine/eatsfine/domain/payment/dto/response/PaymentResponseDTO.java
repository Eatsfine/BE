package com.eatsfine.eatsfine.domain.payment.dto.response;

import com.eatsfine.eatsfine.domain.payment.enums.PaymentMethod;
import com.eatsfine.eatsfine.domain.payment.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class PaymentResponseDTO {

    public record PaymentRequestResultDTO(
            Long paymentId,
            Long bookingId,
            String orderId,
            BigDecimal amount,
            LocalDateTime requestedAt) {
    }

    public record CancelPaymentResultDTO(
            Long paymentId,
            String orderId,
            String paymentKey,
            String status,
            LocalDateTime canceledAt) {
    }

    public record PaymentHistoryResultDTO(
            Long paymentId,
            Long bookingId,
            String restaurantName,
            BigDecimal amount,
            String paymentType,
            String paymentMethod,
            String paymentProvider,
            String status,
            LocalDateTime approvedAt) {
    }

    public record PaymentListResponseDTO(
            List<PaymentHistoryResultDTO> payments,
            PaginationDTO pagination) {
    }

    public record PaginationDTO(
            Integer currentPage,
            Integer totalPages,
            Long totalCount) {
    }

    public record PaymentDetailResultDTO(
            Long paymentId,
            Long bookingId,
            String restaurantName,
            String paymentMethod,
            String paymentProvider,
            BigDecimal amount,
            String paymentType,
            String status,
            LocalDateTime requestedAt,
            LocalDateTime approvedAt,
            String receiptUrl,
            String refundInfo) {
    }
}
