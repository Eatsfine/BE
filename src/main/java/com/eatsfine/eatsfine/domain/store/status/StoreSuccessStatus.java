package com.eatsfine.eatsfine.domain.store.status;

import com.eatsfine.eatsfine.global.apiPayload.code.BaseCode;
import com.eatsfine.eatsfine.global.apiPayload.code.ReasonDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum StoreSuccessStatus implements BaseCode {

    _STORE_FOUND(HttpStatus.OK, "STORE200", "성공적으로 가게를 찾았습니다."),

    _STORE_SEARCH_SUCCESS(HttpStatus.OK, "STORE2002", "성공적으로 가게를 검색했습니다."),

    _STORE_DETAIL_FOUND(HttpStatus.OK, "STORE2003", "성공적으로 가게 상세 리뷰를 조회했습니다."),

    _STORE_CREATED(HttpStatus.CREATED, "STORE201", "성공적으로 가게를 등록했습니다."),

    _STORE_UPDATE_SUCCESS(HttpStatus.OK, "STORE2004", "성공적으로 가게 기본 정보를 수정했습니다."),

    _STORE_MAIN_IMAGE_UPLOAD_SUCCESS(HttpStatus.OK, "STORE2005", "성공적으로 가게 대표 이미지를 업로드했습니다.")
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
