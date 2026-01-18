package com.eatsfine.eatsfine.domain.table_layout.exception.status;

import com.eatsfine.eatsfine.global.apiPayload.code.BaseCode;
import com.eatsfine.eatsfine.global.apiPayload.code.ReasonDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum TableLayoutSuccessStatus implements BaseCode {

    _LAYOUT_CREATED(HttpStatus.CREATED, "LAYOUT201", "성공적으로 배치도를 생성했습니다."),

    _LAYOUT_FOUND(HttpStatus.OK, "LAYOUT200", "성공적으로 배치도를 조회했습니다."),

    _LAYOUT_NO_CONTENT(HttpStatus.NO_CONTENT, "LAYOUT204", "조회된 배치도가 없습니다."),
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
