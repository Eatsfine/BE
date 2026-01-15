package com.eatsfine.eatsfine.domain.businesshours.status;

import com.eatsfine.eatsfine.global.apiPayload.code.BaseErrorCode;
import com.eatsfine.eatsfine.global.apiPayload.code.ErrorReasonDto;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum BusinessHoursErrorStatus implements BaseErrorCode {

    _DUPLICATE_DAY_OF_WEEK(HttpStatus.BAD_REQUEST, "BUSINESS_HOURS4001", "요일이 중복되었습니다."),
    _BUSINESS_HOURS_NOT_COMPLETE(HttpStatus.BAD_REQUEST, "BUSINESS_HOURS4002", "영업일은 7일 모두 입력되어야 합니다."),
    _INVALID_BUSINESS_TIME(HttpStatus.BAD_REQUEST, "BUSINESS_HOURS4003", "영업 시작 시간은 마감 시간보다 빨라야 합니다."),
    _INVALID_OPEN_DAY(HttpStatus.BAD_REQUEST, "BUSINESS_HOURS_4004", "영업일에는 영업시간 및 마감 시간이 존재해야 합니다."),
    _INVALID_CLOSED_DAY(HttpStatus.BAD_REQUEST, "BUSINESS_HOURS_4005", "휴무일에는 영업시간이 존재할 수 없습니다."),
    _BUSINESS_HOURS_DAY_NOT_FOUND(HttpStatus.NOT_FOUND, "BUSINESS_HOURS_404", "해당 요일이 존재하지 않습니다.")
    ;


    private final HttpStatus httpStatus;
    private final String code;
    private final String message;


    @Override
    public ErrorReasonDto getReason() {
        return com.eatsfine.eatsfine.global.apiPayload.code.ErrorReasonDto.builder()
                .isSuccess(false)
                .code(code)
                .message(message)
                .build();
    }

    @Override
    public ErrorReasonDto getReasonHttpStatus() {
        return com.eatsfine.eatsfine.global.apiPayload.code.ErrorReasonDto.builder()
                .httpStatus(httpStatus)
                .isSuccess(false)
                .code(code)
                .message(message)
                .build();
    }
}
