package com.eatsfine.domain.region.status;

import com.eatsfine.global.apipayload.code.BaseErrorCode;
import com.eatsfine.global.apipayload.code.ErrorReasonDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum RegionSuccessStatus implements BaseErrorCode {

    _REGION_FOUND(HttpStatus.OK, "REGION200", "성공적으로 주소를 찾았습니다."),
    ;


    private final HttpStatus httpStatus;
    private final String code;
    private final String message;


    @Override
    public ErrorReasonDto getReason() {
        return com.eatsfine.global.apipayload.code.ErrorReasonDto.builder()
                .isSuccess(false)
                .code(code)
                .message(message)
                .build();
    }

    @Override
    public ErrorReasonDto getReasonHttpStatus() {
        return com.eatsfine.global.apipayload.code.ErrorReasonDto.builder()
                .httpStatus(httpStatus)
                .isSuccess(false)
                .code(code)
                .message(message)
                .build();
    }
}
