package com.eatsfine.eatsfine.domain.user.status;

import com.eatsfine.eatsfine.global.apiPayload.code.BaseErrorCode;
import com.eatsfine.eatsfine.global.apiPayload.code.ErrorReasonDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum UserErrorStatus implements BaseErrorCode {

    // 멤버 관련 에러
    MEMBER_NOT_FOUND(HttpStatus.BAD_REQUEST, "MEMBER4001", "사용자가 없습니다."),
    NAME_NOT_EXIST(HttpStatus.BAD_REQUEST, "MEMBER4002", "이름은 필수 입니다."),
    EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "MEMBER4003", "이미 존재하는 이메일입니다."),
    INVALID_PASSWORD(HttpStatus.BAD_REQUEST, "MEMBER4004", "비밀번호가 올바르지 않습니다."),

    // 토큰 관련 에러
    INVALID_TOKEN(HttpStatus.NOT_FOUND, "TOKEN4001", "토큰이 없습니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "TOKEN4002", "토큰이 만료되었습니다."),
    REFRESH_TOKEN_NOT_ISSUED(HttpStatus.INTERNAL_SERVER_ERROR, "TOKEN5001", "리프레시 토큰이 발급되지 않았습니다."),

    // 사장 인증 관련 에러
    ALREADY_OWNER(HttpStatus.CONFLICT, "OWNER409", "이미 사장 회원입니다."),
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
