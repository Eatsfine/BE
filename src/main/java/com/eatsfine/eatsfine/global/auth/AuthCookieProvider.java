package com.eatsfine.eatsfine.global.auth;

import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class AuthCookieProvider {
    public ResponseCookie refreshTokenCookie(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new IllegalArgumentException("refreshToken must not be blank");
        }

        return ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .domain(".eatsfine.co.kr") 
                .path("/")
                .maxAge(Duration.ofDays(14))
                .build();
    }

    public ResponseCookie clearRefreshTokenCookie() {
        return ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .domain(".eatsfine.co.kr")
                .path("/")
                .maxAge(0) // 수명을 0으로 설정하여 즉시 삭제
                .build();
    }
}
