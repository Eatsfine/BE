package com.eatsfine.eatsfine.domain.tableblock.exception.status;

import com.eatsfine.eatsfine.global.apiPayload.code.BaseErrorCode;
import com.eatsfine.eatsfine.global.apiPayload.code.ErrorReasonDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum TableBlockErrorStatus implements BaseErrorCode {
    _INVALID_SLOT_STATUS(HttpStatus.BAD_REQUEST, "BLOCK400_1", "유효하지 않은 슬롯 상태입니다. BLOCKED 또는 AVAILABLE만 가능합니다."),
    _CANNOT_BLOCK_BOOKED_SLOT(HttpStatus.BAD_REQUEST, "BLOCK400_2", "이미 예약된 시간대는 차단할 수 없습니다."),
    _CANNOT_UNBLOCK_BOOKED_SLOT(HttpStatus.BAD_REQUEST, "BLOCK400_3", "이미 예약된 시간대는 차단 해제 할 수 없습니다."),
    _CANNOT_UNBLOCK_BREAK_TIME(HttpStatus.BAD_REQUEST, "BLOCK400_4", "브레이크타임은 영업시간 설정에서 변경할 수 있습니다."),
    _TABLE_BLOCK_NOT_FOUND(HttpStatus.NOT_FOUND, "BLOCK404_1", "해당 시간대에 차단 내역을 찾을 수 없습니다."),
    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    @Override
    public ErrorReasonDto getReason() {
        return ErrorReasonDto.builder()
                .isSuccess(false)
                .message(message)
                .code(code)
                .build();
    }

    @Override
    public ErrorReasonDto getReasonHttpStatus() {
        return ErrorReasonDto.builder()
                .isSuccess(false)
                .httpStatus(httpStatus)
                .message(message)
                .code(code)
                .build();
    }
}
