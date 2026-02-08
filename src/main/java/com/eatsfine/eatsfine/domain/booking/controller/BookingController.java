package com.eatsfine.eatsfine.domain.booking.controller;

import com.eatsfine.eatsfine.domain.booking.dto.request.BookingRequestDTO;
import com.eatsfine.eatsfine.domain.booking.dto.response.BookingResponseDTO;
import com.eatsfine.eatsfine.domain.booking.service.BookingCommandService;
import com.eatsfine.eatsfine.domain.booking.service.BookingQueryService;
import com.eatsfine.eatsfine.domain.booking.status.BookingSuccessStatus;
import com.eatsfine.eatsfine.domain.user.exception.UserException;
import com.eatsfine.eatsfine.domain.user.repository.UserRepository;
import com.eatsfine.eatsfine.domain.user.status.UserErrorStatus;
import com.eatsfine.eatsfine.global.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
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


    @Operation(summary = "예약 생성",
            description = "가게,날짜,시간,인원,테이블 정보를 입력받아 예약을 생성합니다.")
    @PostMapping("/stores/{storeId}/bookings")
    public ApiResponse<BookingResponseDTO.CreateBookingResultDTO> createBooking(
            @PathVariable Long storeId,
            @RequestBody @Valid BookingRequestDTO.CreateBookingDTO dto,
            @AuthenticationPrincipal User user
    ) {

        String email = user.getUsername();
        com.eatsfine.eatsfine.domain.user.entity.User userEntity = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserException(UserErrorStatus.MEMBER_NOT_FOUND));

        return ApiResponse.onSuccess(bookingCommandService.createBooking(userEntity.getId(), storeId, dto));
    }

    //불필요한 api 삭제
//    @Operation(summary = "예약 완료 처리",
//            description = "결제 완료 후 결제 정보를 입력받아 예약 상태를 업데이트합니다. 주의) 외부에서 이 API를 호출하지 않고  " +
//                    "POST /api/v1/payments/confirm API 호출 후 내부적으로 이 API의 로직을 실행합니다.")
//    @PatchMapping("/bookings/{bookingId}/payments-confirm")
//    public ApiResponse<BookingResponseDTO.ConfirmPaymentResultDTO> confirmPayment(
//            @PathVariable Long bookingId,
//            @RequestBody @Valid BookingRequestDTO.PaymentConfirmDTO dto
//    ) {
//
//        return ApiResponse.onSuccess(bookingCommandService.confirmPayment(bookingId,dto));
//    }

    @Operation(summary = "예약 취소",
            description = "예약을 취소하고 환불을 진행합니다.")
    @PatchMapping("/bookings/{bookingId}/cancel")
    public ApiResponse<BookingResponseDTO.CancelBookingResultDTO> cancelBooking(
            @PathVariable Long bookingId,
            @RequestBody @Valid BookingRequestDTO.CancelBookingDTO dto,
            @AuthenticationPrincipal User user
    ) {
        String email = user.getUsername();
        com.eatsfine.eatsfine.domain.user.entity.User userEntity = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserException(UserErrorStatus.MEMBER_NOT_FOUND));

        return ApiResponse.of(BookingSuccessStatus._BOOKING_CANCELED,
                bookingCommandService.cancelBooking(userEntity.getId(), bookingId, dto));
    }


    @Operation(summary = "예약 내역 조회",
            description = "마이페이지에서 나의 예약 내역을 조회합니다.")
    @GetMapping("/users/bookings")
    public ApiResponse<BookingResponseDTO.BookingPreviewListDTO> getMyBookings(
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(name = "page", defaultValue = "1") Integer page,
            @AuthenticationPrincipal User user
    ) {
        String email = user.getUsername();
        com.eatsfine.eatsfine.domain.user.entity.User userEntity = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserException(UserErrorStatus.MEMBER_NOT_FOUND));

        // 서비스 호출 시 page - 1을 넘겨서 0-based index로 맞춰줍니다.
        return ApiResponse.of(BookingSuccessStatus._BOOKING_FOUND,
                bookingQueryService.getBookingList(userEntity.getId(), status, page - 1));
    }
}
