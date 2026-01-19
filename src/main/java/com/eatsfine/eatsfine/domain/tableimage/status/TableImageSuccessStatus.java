package com.eatsfine.eatsfine.domain.tableimage.status;

import com.eatsfine.eatsfine.global.apiPayload.code.BaseCode;
import com.eatsfine.eatsfine.global.apiPayload.code.ReasonDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum TableImageSuccessStatus implements BaseCode {

        _STORE_TABLE_IMAGE_UPLOAD_SUCCESS(HttpStatus.OK, "TABLE_IMAGE200", "성공적으로 가게 테이블 이미지를 업로드했습니다."),

        _STORE_TABLE_IMAGE_GET_SUCCESS(HttpStatus.OK, "TABLE_IMAGE2001", "성공적으로 가게 테이블 이미지를 조회했습니다."),

        _STORE_TABLE_IMAGE_DELETE_SUCCESS(HttpStatus.OK, "TABLE_IMAGE2002", "성공적으로 가게 테이블 이미지를 삭제했습니다.")
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


