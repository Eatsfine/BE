package com.eatsfine.eatsfine.domain.booking.service;

import com.eatsfine.eatsfine.domain.booking.dto.response.BookingResponseDTO;

import java.time.LocalDate;
import java.time.LocalTime;

public interface BookingQueryService {

    BookingResponseDTO.TimeSlotListDTO getAvailableTimeSlots(Long storeId, LocalDate date, Integer partySize,Boolean isSplitAccepted);

    BookingResponseDTO.AvailableTableListDTO getAvailableTables(Long storeId, LocalDate date, LocalTime time, Integer partySize,Boolean isSplitAccepted, String seatsType);
}
