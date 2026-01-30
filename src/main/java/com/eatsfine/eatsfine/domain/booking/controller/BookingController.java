package com.eatsfine.eatsfine.domain.booking.controller;

import com.eatsfine.eatsfine.domain.booking.dto.request.BookingRequestDTO;
import com.eatsfine.eatsfine.domain.booking.dto.response.BookingResponseDTO;
import com.eatsfine.eatsfine.domain.booking.service.BookingCommandService;
import com.eatsfine.eatsfine.domain.booking.service.BookingQueryService;
import com.eatsfine.eatsfine.domain.user.entity.User;
import com.eatsfine.eatsfine.domain.user.repository.UserRepository;
import com.eatsfine.eatsfine.global.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.*;



@Tag(name = "Booking", description = "예약 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class BookingController {

    private final BookingQueryService bookingQueryService;
    private final BookingCommandService bookingCommandService;
    private final UserRepository userRepository;

    @Operation(summary = "1단계: 예약 가능 시간대 조회"
            , description = "가게, 날짜, 인원수, 테이블 분리 가능 여부를 입력받아 예약 가능한 시간 목록 반환")
    @GetMapping("/stores/{storeId}/bookings/available-times")
    public ApiResponse<BookingResponseDTO.TimeSlotListDTO> getAvailableTimes(
            @ParameterObject @ModelAttribute @Valid BookingRequestDTO.GetAvailableTimeDTO dto,
            @PathVariable Long storeId
            ) {

        return ApiResponse.onSuccess(bookingQueryService.getAvailableTimeSlots(storeId, dto));
    }

    @Operation(summary = "2단계: 예약 가능 테이블 조회"
            , description = "선택한 시간대에 예약 가능한 구체적인 테이블 목록을 반환")
    @GetMapping("/stores/{storeId}/bookings/available-tables")
    public ApiResponse<BookingResponseDTO.AvailableTableListDTO> getAvailableTables(
             @PathVariable Long storeId,
             @ParameterObject @ModelAttribute @Valid BookingRequestDTO.GetAvailableTableDTO dto
            ) {

        return ApiResponse.onSuccess(bookingQueryService.getAvailableTables(storeId, dto));
    }

    @Operation(summary = "예약 생성" ,
            description = "가게,날짜,시간,인원,테이블 정보를 입력받아 예약을 생성합니다.")
    @PostMapping("/stores/{storeId}/bookings")
    public ApiResponse<BookingResponseDTO.CreateBookingResultDTO> createBooking(
            @PathVariable Long storeId,
            @RequestBody @Valid BookingRequestDTO.CreateBookingDTO dto
            ) {

        User user = userRepository.findById(1L).orElseThrow(); // 임시로 임의의 유저 사용
        return ApiResponse.onSuccess(bookingCommandService.createBooking(user, storeId, dto));
    }

    @Operation(summary = "결제 완료 처리",
            description = "결제 완료 후 결제 정보를 입력받아 예약 상태를 업데이트합니다.")
    @PatchMapping("/bookings/{bookingId}/payments-confirm")
    public ApiResponse<BookingResponseDTO.ConfirmPaymentResultDTO> confirmPayment(
            @PathVariable Long bookingId,
            @RequestBody @Valid BookingRequestDTO.PaymentConfirmDTO dto
    ) {

        return ApiResponse.onSuccess(bookingCommandService.confirmPayment(bookingId,dto));
    }

}
