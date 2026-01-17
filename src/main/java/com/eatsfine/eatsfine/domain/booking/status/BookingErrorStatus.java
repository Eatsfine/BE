package com.eatsfine.eatsfine.domain.booking.status;

import com.eatsfine.eatsfine.global.apiPayload.code.BaseErrorCode;
import com.eatsfine.eatsfine.global.apiPayload.code.ErrorReasonDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum BookingErrorStatus implements BaseErrorCode {

    _STORE_NOT_FOUND(HttpStatus.NOT_FOUND, "STORE404", "해당 가게를 찾을 수 없습니다."),
    _BUSINESS_HOURS_NOT_FOUND(HttpStatus.NOT_FOUND, "STORE4041", "해당 날짜의 영업시간 정보가 없습니다."),
    _LAYOUT_NOT_FOUND(HttpStatus.NOT_FOUND, "LAYOUT404", "가게의 활성화된 테이블 레이아웃이 없습니다."),
    _BOOKING_NOT_FOUND(HttpStatus.NOT_FOUND, "BOOKING404", "예약 정보를 찾을 수 없습니다."),
    _INVALID_PARTY_SIZE(HttpStatus.BAD_REQUEST, "BOOKING4001", "인원 설정이 잘못되었습니다."),
    _ALREADY_RESERVED_TABLE(HttpStatus.CONFLICT, "BOOKING4091", "선택하신 테이블 중 이미 예약된 테이블이 포함되어 있습니다."),
    _ALREADY_CONFIRMED(HttpStatus.BAD_REQUEST,"BOOKING4002", "이미 확정된 예약입니다."),
    _PAYMENT_AMOUNT_MISMATCH(HttpStatus.BAD_REQUEST, "BOOKING4003", "결제 금액이 일치하지 않습니다.");


    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    @Override
    public ErrorReasonDto getReason() {
        return ErrorReasonDto.builder()
                .isSuccess(false)
                .code(code)
                .message(message)
                .build();
    }

    @Override
    public ErrorReasonDto getReasonHttpStatus() {
        return ErrorReasonDto.builder()
                .httpStatus(httpStatus)
                .isSuccess(false)
                .code(code)
                .message(message)
                .build();
    }
}
