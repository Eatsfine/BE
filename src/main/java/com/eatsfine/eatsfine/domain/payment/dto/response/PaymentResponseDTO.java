package com.eatsfine.eatsfine.domain.payment.dto.response;


import java.time.LocalDateTime;
import java.util.List;

public class PaymentResponseDTO {

    public record PaymentRequestResultDTO(
            Long paymentId,
            Long bookingId,
            String orderId,
            Integer amount,
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
            String storeName,
            Integer amount,
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
            String storeName,
            String paymentMethod,
            String paymentProvider,
            Integer amount,
            String paymentType,
            String status,
            LocalDateTime requestedAt,
            LocalDateTime approvedAt,
            String receiptUrl,
            String refundInfo) {
    }

    public record PaymentSuccessResultDTO(
            Long paymentId,
            String status,
            LocalDateTime approvedAt,
            String orderId,
            Integer amount,
            String paymentMethod,
            String paymentProvider,
            String receiptUrl) {
    }
}
