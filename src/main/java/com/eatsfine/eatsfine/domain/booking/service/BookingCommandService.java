package com.eatsfine.eatsfine.domain.booking.service;

import com.eatsfine.eatsfine.domain.booking.dto.request.BookingRequestDTO;
import com.eatsfine.eatsfine.domain.booking.dto.response.BookingResponseDTO;
import com.eatsfine.eatsfine.domain.user.entity.User;
import jakarta.validation.Valid;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface BookingCommandService {

    BookingResponseDTO.CreateBookingResultDTO createBooking(User user, Long storeId, BookingRequestDTO.CreateBookingDTO dto);

    BookingResponseDTO.ConfirmPaymentResultDTO confirmPayment(Long BookingId, BookingRequestDTO.PaymentConfirmDTO dto);
}
