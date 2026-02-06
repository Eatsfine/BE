package com.eatsfine.eatsfine.domain.booking.service;

import com.eatsfine.eatsfine.domain.booking.dto.request.BookingRequestDTO;
import com.eatsfine.eatsfine.domain.booking.dto.response.BookingResponseDTO;
import jakarta.validation.Valid;
import org.springframework.security.core.userdetails.User;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface BookingCommandService {

    BookingResponseDTO.CreateBookingResultDTO createBooking(Long userId, Long storeId, BookingRequestDTO.CreateBookingDTO dto);

    BookingResponseDTO.ConfirmPaymentResultDTO confirmPayment(Long BookingId, BookingRequestDTO.PaymentConfirmDTO dto);

    BookingResponseDTO.CancelBookingResultDTO cancelBooking(Long bookingId, BookingRequestDTO.CancelBookingDTO dto);

    BookingResponseDTO.OwnerCancelBookingResultDTO cancelBookingByOwner(Long storeId, Long tableId, Long bookingId);
}
