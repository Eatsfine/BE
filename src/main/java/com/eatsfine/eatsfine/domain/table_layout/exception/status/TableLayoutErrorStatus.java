package com.eatsfine.eatsfine.domain.table_layout.exception.status;

import com.eatsfine.eatsfine.global.apiPayload.code.BaseErrorCode;
import com.eatsfine.eatsfine.global.apiPayload.code.ErrorReasonDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum TableLayoutErrorStatus implements BaseErrorCode {

    _LAYOUT_NOT_FOUND(HttpStatus.NOT_FOUND, "LAYOUT404", "배치도를 찾을 수 없습니다."),

    _LAYOUT_FORBIDDEN(HttpStatus.FORBIDDEN, "LAYOUT403", "해당 가게의 소유자만 접근 가능합니다."),

    _CANNOT_DELETE_LAYOUT_WITH_FUTURE_BOOKINGS(HttpStatus.BAD_REQUEST, "LAYOUT400_2", "미래 예약이 있는 배치도는 삭제할 수 없습니다. 모든 예약이 완료된 후 재생성해주세요.")
    ;

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
