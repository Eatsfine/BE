package com.eatsfine.eatsfine.domain.booking.controller;

import com.eatsfine.eatsfine.domain.booking.dto.response.BookingResponseDTO;
import com.eatsfine.eatsfine.domain.booking.service.BookingQueryService;
import com.eatsfine.eatsfine.global.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Tag(name = "Booking", description = "예약 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class BookingController {

    private final BookingQueryService bookingQueryService;

    @Operation(summary = "1단계: 예약 가능 시간대 조회"
            , description = "가게, 날짜, 인원수, 테이블 분리 가능 여부를 입력받아 예약 가능한 시간 목록 반환")
    @GetMapping("/stores/{storeId}/bookings/available-times")
    public ApiResponse<BookingResponseDTO.TimeSlotListDTO> getAvailableTimes(
            @PathVariable Long storeId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
            @RequestParam Integer partySize,
            @RequestParam(defaultValue = "false") Boolean isSplitAccepted) {

        return ApiResponse.onSuccess(bookingQueryService.getAvailableTimeSlots(storeId, date, partySize,isSplitAccepted));
    }

    @Operation(summary = "2단계: 예약 가능 테이블 조회"
            , description = "선택한 시간대에 예약 가능한 구체적인 테이블 목록을 반환")
    @GetMapping("/stores/{storeId}/bookings/available-tables")
    public ApiResponse<BookingResponseDTO.AvailableTableListDTO> getAvailableTables(
            @PathVariable Long storeId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
            @RequestParam @DateTimeFormat(pattern = "HH:mm") LocalTime time,
            @RequestParam Integer partySize,
            @RequestParam(required = false) String seatsType) {

        return ApiResponse.onSuccess(bookingQueryService.getAvailableTables(storeId, date, time, partySize,seatsType));
    }

}
