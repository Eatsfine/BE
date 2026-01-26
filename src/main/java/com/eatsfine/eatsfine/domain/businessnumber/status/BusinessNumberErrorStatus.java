package com.eatsfine.eatsfine.domain.businessnumber.status;

import com.eatsfine.eatsfine.global.apiPayload.code.BaseErrorCode;
import com.eatsfine.eatsfine.global.apiPayload.code.ErrorReasonDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum BusinessNumberErrorStatus implements BaseErrorCode {

    _API_COMMUNICATION_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "BUSINESS_NUMBER500", "공공데이터 서버와 통신 중 오류가 발생했습니다."),

    _INVALID_BUSINESS_NUMBER(HttpStatus.BAD_REQUEST, "BUSINESS_NUMBER400", "사업자 번호가 유효하지 않습니다."),
    ;

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
