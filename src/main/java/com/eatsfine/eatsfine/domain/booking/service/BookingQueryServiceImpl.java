package com.eatsfine.eatsfine.domain.booking.service;

import com.eatsfine.eatsfine.domain.booking.dto.response.BookingResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;

@Service
@RequiredArgsConstructor
public class BookingQueryServiceImpl implements BookingQueryService {

    @Override
    public BookingResponseDTO.TimeSlotListDTO getAvailableTimeSlots(Long storeId, LocalDate date, Integer partySize) {
        return null;
    }

    @Override
    public BookingResponseDTO.AvailableTableListDTO getAvailableTables(Long storeId, LocalDate date, LocalTime time, Integer partySize) {
        return null;
    }
}
