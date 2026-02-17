package com.eatsfine.eatsfine.domain.payment.status;

import com.eatsfine.eatsfine.global.apiPayload.code.BaseErrorCode;
import com.eatsfine.eatsfine.global.apiPayload.code.ErrorReasonDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum PaymentErrorStatus implements BaseErrorCode {

    _PAYMENT_INVALID_DEPOSIT(HttpStatus.BAD_REQUEST, "PAYMENT4001", "예약금이 유효하지 않습니다."),
    _PAYMENT_INVALID_AMOUNT(HttpStatus.BAD_REQUEST, "PAYMENT4002", "결제 금액이 일치하지 않습니다."),
    _PAYMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "PAYMENT4003", "결제 정보를 찾을 수 없습니다."),
    _BOOKING_NOT_FOUND(HttpStatus.NOT_FOUND, "BOOKING4001", "예약을 찾을 수 없습니다."),
    _PAYMENT_ACCESS_DENIED(HttpStatus.FORBIDDEN, "PAYMENT4031", "해당 결제 정보에 접근할 권한이 없습니다.");

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
