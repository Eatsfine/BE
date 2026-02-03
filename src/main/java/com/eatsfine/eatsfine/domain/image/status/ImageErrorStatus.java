package com.eatsfine.eatsfine.domain.image.status;

import com.eatsfine.eatsfine.global.apiPayload.code.BaseErrorCode;
import com.eatsfine.eatsfine.global.apiPayload.code.ErrorReasonDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ImageErrorStatus implements BaseErrorCode {
    EMPTY_FILE(HttpStatus.BAD_REQUEST, "IMAGE4001", "업로드할 파일이 비어 있습니다."),
    INVALID_FILE_TYPE(HttpStatus.BAD_REQUEST, "IMAGE4002", "지원하지 않는 파일 형식입니다."),
    S3_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "IMAGE5001", "이미지 업로드에 실패했습니다."),
    _IMAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "IMAGE404", "해당하는 이미지가 존재하지 않습니다."),
    _INVALID_IMAGE_KEY(HttpStatus.BAD_REQUEST, "IMAGE4003", "유효하지 않은 이미지 키입니다."),
    _INVALID_S3_DIRECTORY(HttpStatus.BAD_REQUEST, "IMAGE4004", "유효하지 않은 S3 디렉토리입니다."),
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
                .httpStatus(httpStatus)
                .isSuccess(false)
                .code(code)
                .message(message)
                .build();
    }
}
