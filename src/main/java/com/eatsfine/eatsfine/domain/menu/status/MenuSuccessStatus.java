package com.eatsfine.eatsfine.domain.menu.status;

import com.eatsfine.eatsfine.global.apiPayload.code.BaseCode;
import com.eatsfine.eatsfine.global.apiPayload.code.ReasonDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum MenuSuccessStatus implements BaseCode {


    _MENU_IMAGE_UPLOAD_SUCCESS(HttpStatus.CREATED, "MENU201", "메뉴 이미지 업로드 성공"),

    _MENU_IMAGE_DELETE_SUCCESS(HttpStatus.OK, "MENU200", "메뉴 이미지 삭제 성공"),


    _MENU_CREATE_SUCCESS(HttpStatus.CREATED, "MENU202", "메뉴 생성 성공"),
    _MENU_DELETE_SUCCESS(HttpStatus.OK, "MENU2002", "메뉴 삭제 성공"),
    _MENU_UPDATE_SUCCESS(HttpStatus.OK, "MENU2003", "메뉴 수정 성공"),
    _SOLD_OUT_UPDATE_SUCCESS(HttpStatus.OK, "MENU2005", "품절 여부 변경 성공"),
    _MENU_LIST_SUCCESS(HttpStatus.OK, "MENU2004", "메뉴 조회 성공"),

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
