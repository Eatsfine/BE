package com.eatsfine.eatsfine.domain.businesshours.status;

import com.eatsfine.eatsfine.global.apiPayload.code.BaseCode;
import com.eatsfine.eatsfine.global.apiPayload.code.ReasonDto;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum BusinessHoursSuccessStatus implements BaseCode {

    _UPDATE_BUSINESS_HOURS_SUCCESS(HttpStatus.OK, "BUSINESS_HOURS200", "영업시간이 성공적으로 수정되었습니다."),
    _UPDATE_BREAKTIME_SUCCESS(HttpStatus.OK, "BUSINESS_HOURS2001", "브레이크타임이 성공적으로 설정되었습니다.")
    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;


    @Override
    public ReasonDto getReason() {
        return ReasonDto.builder()
                .isSuccess(false)
                .code(code)
                .message(message)
                .build();
    }

    @Override
    public ReasonDto getReasonHttpStatus() {
        return ReasonDto.builder()
                .httpStatus(httpStatus)
                .isSuccess(false)
                .code(code)
                .message(message)
                .build();
    }
}
