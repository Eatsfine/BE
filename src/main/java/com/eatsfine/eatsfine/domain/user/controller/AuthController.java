package com.eatsfine.eatsfine.domain.user.controller;

import com.eatsfine.eatsfine.domain.user.dto.response.UserResponseDto;
import com.eatsfine.eatsfine.domain.user.service.authService.AuthTokenService;
import com.eatsfine.eatsfine.global.apiPayload.ApiResponse;
import com.eatsfine.eatsfine.global.auth.AuthCookieProvider;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthTokenService authTokenService;
    private final AuthCookieProvider authCookieProvider;

    @PostMapping("/reissue")
    public ResponseEntity<ApiResponse<UserResponseDto.AccessTokenResponse>> reissue(
            @CookieValue(value = "refreshToken", required = false) String refreshToken,
            HttpServletResponse response
    ) {
        log.info("[REISSUE API] 재발급 요청 받음. refreshToken={}", refreshToken);

        AuthTokenService.ReissueResult result = authTokenService.reissue(refreshToken);

        ResponseCookie refreshCookie = authCookieProvider.refreshTokenCookie(result.refreshToken());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

        log.info("[REISSUE API] 재발급 성공. 새 쿠키 설정 완료");

        return ResponseEntity.ok(
                ApiResponse.onSuccess(new UserResponseDto.AccessTokenResponse(result.accessToken()))
        );
    }
}