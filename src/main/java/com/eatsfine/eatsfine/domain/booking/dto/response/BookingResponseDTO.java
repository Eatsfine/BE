package com.eatsfine.eatsfine.domain.booking.dto.response;

import com.eatsfine.eatsfine.domain.booking.enums.BookingStatus;
import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public class BookingResponseDTO {

    @Builder
    public record TimeSlotListDTO(
            List<LocalTime> availableTimes
    ) {}

    @Builder
    public record AvailableTableListDTO(
            int rows,
            int cols,
            List<TableInfoDTO> tables
    ) {}

    @Builder
    public record TableInfoDTO(
            Long tableId,
            String tableNumber,
            Integer tableSeats,
            String seatsType,
            int gridX,
            int gridY,
            int widthSpan,
            int heightSpan
    ){}

    @Builder
    public record CreateBookingResultDTO(
            Long bookingId,
        //    Long paymentId,  // 결제 정보 추후 포함
            String status,
            String storeName,
            LocalDate date,
            LocalTime time,
            Integer partySize,
            Integer totalDeposit,
            List<BookingResultTableDTO> tables,
            LocalDateTime createdAt, // 예약 생성 시간
            Long paymentId,  // 결제 ID
            String orderId // 주문 ID
    ){}

    @Builder
    public record BookingResultTableDTO(
            Long tableId,
            String tableNumber,
            Integer tableSeats,
            String seatsType
    ){}


    @Builder
    public record ConfirmPaymentResultDTO(
            Long bookingId,
            String status,         // CONFIRMED
            String paymentKey,     // PG사 결제 키
            Integer amount        // 최종 결제 금액
    ){}

    @Builder
    public record CancelBookingResultDTO(
            Long bookingId,
            String status,         // CANCELED
            String cancelReason,   // 취소 사유
            LocalDateTime canceledAt, // 취소 시간
            Integer refundAmount    // 환불 금액
    ){}
}
