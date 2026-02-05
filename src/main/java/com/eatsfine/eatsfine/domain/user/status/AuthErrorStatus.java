package com.eatsfine.eatsfine.domain.user.status;

import com.eatsfine.eatsfine.global.apiPayload.code.BaseErrorCode;
import com.eatsfine.eatsfine.global.apiPayload.code.ErrorReasonDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum AuthErrorStatus implements BaseErrorCode {

    OAUTH2_EMAIL_NOT_FOUND(HttpStatus.BAD_REQUEST, "AUTH4001", "소셜 로그인 이메일을 가져올 수 없습니다."),
    OAUTH2_PROVIDER_NOT_SUPPORTED(HttpStatus.BAD_REQUEST, "AUTH4002", "지원하지 않는 소셜 로그인 제공자입니다."),
    REFRESH_TOKEN_MISSING(HttpStatus.UNAUTHORIZED, "AUTH4005", "리프레시 토큰이 없습니다."),


    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH4003", "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH4004", "토큰이 만료되었습니다."),
    REFRESH_TOKEN_NOT_ISSUED(HttpStatus.INTERNAL_SERVER_ERROR, "AUTH5001", "리프레시 토큰이 발급되지 않았습니다.");

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
