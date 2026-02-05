package com.eatsfine.eatsfine.domain.user.exception.handler;

import com.eatsfine.eatsfine.domain.user.entity.User;
import com.eatsfine.eatsfine.domain.user.enums.SocialType;
import com.eatsfine.eatsfine.domain.user.repository.UserRepository;
import com.eatsfine.eatsfine.global.auth.AuthCookieProvider;
import com.eatsfine.eatsfine.global.config.jwt.JwtTokenProvider;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Transactional
public class CustomOAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthCookieProvider authCookieProvider;

    private static final String CALLBACK_REDIRECT_BASE = "https://eatsfine.co.kr/oauth/callback";
    private static final String LOGIN_ERROR_REDIRECT_BASE = "https://eatsfine.co.kr/login/error";

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
        String provider = oauthToken.getAuthorizedClientRegistrationId(); // google, kakao
        SocialType socialType = SocialType.valueOf(provider.toUpperCase());

        OAuth2User oAuth2User = oauthToken.getPrincipal();

        String email = extractEmail(socialType, oAuth2User);
        if (email == null || email.isBlank()) {
            String failUrl = UriComponentsBuilder.fromUriString(LOGIN_ERROR_REDIRECT_BASE)
                    .queryParam("error", "email_not_found")
                    .build().toUriString();
            response.sendRedirect(failUrl);
            return;
        }

        // DB에서 user 조회
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            String failUrl = UriComponentsBuilder.fromUriString(LOGIN_ERROR_REDIRECT_BASE)
                    .queryParam("error", "user_not_found")
                    .build().toUriString();
            response.sendRedirect(failUrl);
            return;
        }

        // 토큰 발급
        String accessToken = jwtTokenProvider.createAccessToken(email);
        String refreshToken = jwtTokenProvider.createRefreshToken(email);

        // refresh DB 저장
        user.updateRefreshToken(refreshToken);
        userRepository.save(user);

        // refresh 쿠키 세팅
        ResponseCookie refreshCookie = authCookieProvider.refreshTokenCookie(refreshToken);
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

        String redirectUrl = UriComponentsBuilder.fromUriString(CALLBACK_REDIRECT_BASE)
                .queryParam("accessToken", accessToken)
                .build()
                .toUriString();

        response.sendRedirect(redirectUrl);
    }

    @SuppressWarnings("unchecked")
    private String extractEmail(SocialType socialType, OAuth2User oAuth2User) {
        Map<String, Object> attributes = oAuth2User.getAttributes();

        if (socialType == SocialType.GOOGLE) {
            Object email = attributes.get("email");
            return email != null ? String.valueOf(email) : null;
        }

        if (socialType == SocialType.KAKAO) {
            Object kakaoAccountObj = attributes.get("kakao_account");
            if (kakaoAccountObj instanceof Map<?, ?> kakaoAccount) {
                Object email = kakaoAccount.get("email");
                return email != null ? String.valueOf(email) : null;
            }
        }

        return null;
    }
}