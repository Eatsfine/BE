package com.eatsfine.eatsfine.domain.booking.status;

import com.eatsfine.eatsfine.global.apiPayload.code.BaseCode;
import com.eatsfine.eatsfine.global.apiPayload.code.ReasonDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum BookingSuccessStatus implements BaseCode {

    _BOOKING_FOUND(HttpStatus.OK, "BOOKING200", "성공적으로 예약을 조회 했습니다."),

    _BOOKING_DETAIL_FOUND(HttpStatus.FOUND, "BOOKING_DETAIL200", "성공적으로 예약 상세 내역을 조회했습니다."),
    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    @Override
    public ReasonDto getReason() {
        return ReasonDto.builder()
                .isSuccess(true)
                .message(message)
                .code(code)
                .build();
    }

    @Override
    public ReasonDto getReasonHttpStatus() {
        return ReasonDto.builder()
                .isSuccess(true)
                .httpStatus(httpStatus)
                .message(message)
                .code(code)
                .build();
    }

}
