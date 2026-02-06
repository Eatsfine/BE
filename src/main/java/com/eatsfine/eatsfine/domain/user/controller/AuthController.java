package com.eatsfine.eatsfine.domain.user.controller;

import com.eatsfine.eatsfine.domain.user.dto.response.UserResponseDto;
import com.eatsfine.eatsfine.domain.user.entity.User;
import com.eatsfine.eatsfine.domain.user.exception.AuthException;
import com.eatsfine.eatsfine.domain.user.repository.UserRepository;
import com.eatsfine.eatsfine.domain.user.service.authService.AuthTokenService;
import com.eatsfine.eatsfine.domain.user.status.AuthErrorStatus;
import com.eatsfine.eatsfine.global.apiPayload.ApiResponse;
import com.eatsfine.eatsfine.global.auth.AuthCookieProvider;
import com.eatsfine.eatsfine.global.config.jwt.JwtTokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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


@Tag(name = "RefreshToken", description = "refreshToken 재발급 API")
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthTokenService authTokenService;
    private final AuthCookieProvider authCookieProvider;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    @PostMapping("/reissue")
    @Operation(summary = "재발급 API", description = "refreshToken을 재발급 하는 API입니다.")
    public ResponseEntity<ApiResponse<UserResponseDto.AccessTokenResponse>> reissue(
            @CookieValue(value = "refreshToken", required = false) String refreshToken,
            HttpServletResponse response
    ) {
        log.info("[REISSUE API] 재발급 요청 받음. refreshToken={}", refreshToken);

        // refreshToken에서 email 추출
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new AuthException(AuthErrorStatus.REFRESH_TOKEN_MISSING);
        }

        String email = jwtTokenProvider.getEmailFromToken(refreshToken);

        // DB에서 User 조회
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AuthException(AuthErrorStatus.INVALID_TOKEN));

        // 토큰 재발급
        AuthTokenService.ReissueResult result = authTokenService.reissue(refreshToken, user.getRole());

        ResponseCookie refreshCookie = authCookieProvider.refreshTokenCookie(result.refreshToken());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

        log.info("[REISSUE API] 재발급 성공. 새 쿠키 설정 완료");

        return ResponseEntity.ok(
                ApiResponse.onSuccess(new UserResponseDto.AccessTokenResponse(result.accessToken()))
        );
    }
}