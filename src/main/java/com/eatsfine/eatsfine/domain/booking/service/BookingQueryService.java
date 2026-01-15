package com.eatsfine.eatsfine.domain.booking.service;

import com.eatsfine.eatsfine.domain.booking.dto.request.BookingRequestDTO;
import com.eatsfine.eatsfine.domain.booking.dto.response.BookingResponseDTO;

import java.time.LocalDate;
import java.time.LocalTime;

public interface BookingQueryService {

    BookingResponseDTO.TimeSlotListDTO getAvailableTimeSlots(Long storeId, BookingRequestDTO.GetAvailableTimeDTO dto);

    BookingResponseDTO.AvailableTableListDTO getAvailableTables(Long storeId, BookingRequestDTO.GetAvailableTableDTO dto);
}
