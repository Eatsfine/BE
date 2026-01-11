package com.eatsfine.eatsfine.global.apiPayload.code.status;

import com.eatsfine.eatsfine.global.apiPayload.code.BaseErrorCode;
import com.eatsfine.eatsfine.global.apiPayload.code.ErrorReasonDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorStatus implements BaseErrorCode {
    _INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON500", "서버 에러, 관리자에게 문의 바랍니다."),
    _BAD_REQUEST(HttpStatus.BAD_REQUEST, "COMMON400", "잘못된 요청입니다."),
    _UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "COMMON401", "인증이 필요합니다."),
    _FORBIDDEN(HttpStatus.FORBIDDEN, "COMMON403", "금지된 요청입니다."),
    _NOT_FOUND(HttpStatus.NOT_FOUND,"COMMON404","존재하지 않는 요청입니다."),
// 가게 및 예약 관련 에러
    _STORE_NOT_FOUND(HttpStatus.NOT_FOUND, "STORE404", "해당 가게를 찾을 수 없습니다."),
    _BUSINESS_HOURS_NOT_FOUND(HttpStatus.NOT_FOUND, "STORE4041", "해당 날짜의 영업시간 정보가 없습니다."),
    _LAYOUT_NOT_FOUND(HttpStatus.NOT_FOUND, "LAYOUT404", "가게의 활성화된 테이블 레이아웃이 없습니다."),
    _BOOKING_NOT_FOUND(HttpStatus.NOT_FOUND, "BOOKING404", "예약 정보를 찾을 수 없습니다."),
    _INVALID_PARTY_SIZE(HttpStatus.BAD_REQUEST, "BOOKING4001", "인원 설정이 잘못되었습니다.");


    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    @Override
    public ErrorReasonDto getReason() {
        return ErrorReasonDto.builder()
                .isSuccess(true)
                .message(message)
                .code(code)
                .build();
    }

    @Override
    public ErrorReasonDto getReasonHttpStatus() {
        return ErrorReasonDto.builder()
                .httpStatus(httpStatus)
                .isSuccess(true)
                .code(code)
                .message(message)
                .build();
    }

}
