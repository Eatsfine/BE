package com.eatsfine.eatsfine.domain.booking.service;

import com.eatsfine.eatsfine.domain.booking.dto.request.BookingRequestDTO;
import com.eatsfine.eatsfine.domain.booking.dto.response.BookingResponseDTO;
import com.eatsfine.eatsfine.domain.user.entity.User;

import java.time.LocalDate;
import java.time.LocalTime;

public interface BookingQueryService {

    BookingResponseDTO.TimeSlotListDTO getAvailableTimeSlots(Long storeId, BookingRequestDTO.GetAvailableTimeDTO dto);

    BookingResponseDTO.AvailableTableListDTO getAvailableTables(Long storeId, BookingRequestDTO.GetAvailableTableDTO dto);

    BookingResponseDTO.BookingPreviewListDTO getBookingList(User user, String status, Integer page);
}
