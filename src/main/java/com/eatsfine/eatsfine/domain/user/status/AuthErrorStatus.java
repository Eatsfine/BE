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

    // 토큰 관련 에러
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH4003", "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH4004", "토큰이 만료되었습니다."),
    REFRESH_TOKEN_NOT_ISSUED(HttpStatus.INTERNAL_SERVER_ERROR, "AUTH5001", "리프레시 토큰이 발급되지 않았습니다."),

    //소셜 로그인 유저 비번 수정 관련 에러
    OAUTH_PASSWORD_NOT_SUPPORTED(HttpStatus.CONFLICT, "AUTH4010", "소셜 로그인 계정은 비밀번호 변경을 지원하지 않습니다."),

    EMPTY_TOKEN_ROLE(HttpStatus.UNAUTHORIZED, "AUTH407", "토큰 내 권한 정보가 누락되었습니다."),
    // 사장 인증 관련 에러
    ALREADY_OWNER(HttpStatus.CONFLICT, "OWNER409", "이미 사장 회원입니다."),

    // 사장 전용 API 접근 관련 에러
    FORBIDDEN_OWNER(HttpStatus.FORBIDDEN, "AUTH406", "사장님 권한이 필요한 서비스입니다.")
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
