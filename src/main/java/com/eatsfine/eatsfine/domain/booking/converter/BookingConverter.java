package com.eatsfine.eatsfine.domain.booking.converter;

import com.eatsfine.eatsfine.domain.booking.dto.response.BookingResponseDTO;
import com.eatsfine.eatsfine.domain.booking.entity.Booking;
import com.eatsfine.eatsfine.domain.payment.dto.response.PaymentResponseDTO;
import com.eatsfine.eatsfine.domain.store.entity.Store;

import java.math.BigDecimal;
import java.util.List;

public class BookingConverter {

    public static BookingResponseDTO.CreateBookingResultDTO toCreateBookingResultDTO(
            Booking booking, Store store, BigDecimal totalDeposit,
            List<BookingResponseDTO.BookingResultTableDTO> resultTableDTOS,
            PaymentResponseDTO.PaymentRequestResultDTO paymentInfo) {

        return BookingResponseDTO.CreateBookingResultDTO.builder()
                .bookingId(booking.getId())
                .storeName(store.getStoreName())
                .date(booking.getBookingDate())
                .time(booking.getBookingTime())
                .partySize(booking.getPartySize())
                .status(booking.getStatus().name())
                .totalDeposit(totalDeposit)
                .createdAt(booking.getCreatedAt())
                .tables(resultTableDTOS)
                .paymentId(paymentInfo.paymentId())
                .orderId(paymentInfo.orderId())
                .build();
    }
}
