package com.eatsfine.eatsfine.domain.storetable.exception.status;

import com.eatsfine.eatsfine.global.apiPayload.code.BaseCode;
import com.eatsfine.eatsfine.global.apiPayload.code.ReasonDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum StoreTableSuccessStatus implements BaseCode {

    _TABLE_CREATED(HttpStatus.CREATED, "TABLE201_1", "성공적으로 테이블을 생성했습니다."),
    _SLOT_LIST_FOUND(HttpStatus.OK, "TABLE200_1", "테이블 시간 슬롯 조회에 성공했습니다."),
    _TABLE_DETAIL_FOUND(HttpStatus.OK, "TABLE200_2", "테이블 상세 정보 조회에 성공했습니다."),
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

