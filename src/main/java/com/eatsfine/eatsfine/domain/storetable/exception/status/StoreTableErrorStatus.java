package com.eatsfine.eatsfine.domain.storetable.exception.status;

import com.eatsfine.eatsfine.global.apiPayload.code.BaseErrorCode;
import com.eatsfine.eatsfine.global.apiPayload.code.ErrorReasonDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum StoreTableErrorStatus implements BaseErrorCode {

    _TABLE_NOT_FOUND(HttpStatus.NOT_FOUND, "TABLE404_1", "테이블을 찾을 수 없습니다."),
    _TABLE_INVALID_SEAT_RANGE(HttpStatus.BAD_REQUEST, "TABLE400_1", "최소 인원은 최대 인원보다 작거나 같아야 합니다."),
    _TABLE_POSITION_OUT_OF_BOUNDS(HttpStatus.BAD_REQUEST, "TABLE400_2", "테이블 위치가 배치도 그리드 범위를 벗어났습니다."),
    _TABLE_POSITION_OVERLAPS(HttpStatus.BAD_REQUEST, "TABLE400_3", "해당 위치에 이미 다른 테이블이 존재합니다."),
    _TABLE_NOT_BELONGS_TO_STORE(HttpStatus.BAD_REQUEST, "TABLE400_4", "해당 테이블은 해당 가게에 속하지 않습니다."),
    _NO_BUSINESS_HOURS(HttpStatus.NOT_FOUND, "TABLE404_2", "해당 요일의 영업시간 정보를 찾을 수 없습니다."),
    _NO_UPDATE_FIELD(HttpStatus.BAD_REQUEST, "TABLE400_5", "수정할 필드가 최소 하나 이상 필요합니다."),
    _SAME_SEAT_COUNT(HttpStatus.BAD_REQUEST, "TABLE400_6", "기존 좌석 수와 동일합니다."),
    _SAME_SEATS_TYPE(HttpStatus.BAD_REQUEST, "TABLE400_7", "기존 좌석 유형과 동일합니다."),
    _TABLE_HAS_FUTURE_BOOKING(HttpStatus.BAD_REQUEST, "TABLE400_8", "해당 테이블에 존재하는 예약이 있어 삭제할 수 없습니다."),
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
