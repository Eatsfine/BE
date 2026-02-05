package com.eatsfine.eatsfine.domain.user.controller;

import com.eatsfine.eatsfine.domain.user.dto.response.UserResponseDto;
import com.eatsfine.eatsfine.domain.user.service.authService.AuthTokenService;
import com.eatsfine.eatsfine.global.apiPayload.ApiResponse;
import com.eatsfine.eatsfine.global.auth.AuthCookieProvider;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
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
        // 서비스에서 검증 &재발급
        AuthTokenService.ReissueResult result = authTokenService.reissue(refreshToken);

        // refresh 쿠키 갱신
        ResponseCookie refreshCookie = authCookieProvider.refreshTokenCookie(result.refreshToken());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

        // access 응답
        return ResponseEntity.ok(
                ApiResponse.onSuccess(new UserResponseDto.AccessTokenResponse(result.accessToken()))
        );
    }
}