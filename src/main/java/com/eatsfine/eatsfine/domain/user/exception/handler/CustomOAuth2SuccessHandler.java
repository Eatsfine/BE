package com.eatsfine.eatsfine.domain.user.exception.handler;

import com.eatsfine.eatsfine.domain.user.entity.User;
import com.eatsfine.eatsfine.domain.user.enums.SocialType;
import com.eatsfine.eatsfine.domain.user.repository.UserRepository;
import com.eatsfine.eatsfine.global.auth.AuthCookieProvider;
import com.eatsfine.eatsfine.global.config.jwt.JwtTokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger log = LoggerFactory.getLogger(CustomOAuth2SuccessHandler.class);

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

        SocialType socialType;
        try {
            socialType = SocialType.valueOf(provider.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.error("Unknown provider registrationId={}", provider, e);
            redirectFail(response, "unknown_provider");
            return;
        }

        OAuth2User oAuth2User = oauthToken.getPrincipal();
        Map<String, Object> attrs = oAuth2User.getAttributes();

        log.info("[OAuth2 SUCCESS] provider={}, attrs={}", provider, attrs);

        String socialId = extractSocialId(socialType, attrs);
        log.info("[OAuth2 SUCCESS] provider={}, socialId={}", provider, socialId);

        String email = extractEmail(socialType, attrs);
        log.info("[OAuth2 SUCCESS] provider={}, extractedEmail={}", provider, email);

        if (email == null || email.isBlank()) {
            if (socialType == SocialType.KAKAO) {
                logKakaoAccountStatus(attrs);
            }
            redirectFail(response, "email_not_found");
            return;
        }

        // DB에서 user 조회
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            redirectFail(response, "user_not_found");
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

        log.info("[OAuth2 SUCCESS] redirectUrl={}", redirectUrl);
        response.sendRedirect(redirectUrl);
    }

    private void redirectFail(HttpServletResponse response, String errorCode) throws IOException {
        String failUrl = UriComponentsBuilder.fromUriString(LOGIN_ERROR_REDIRECT_BASE)
                .queryParam("error", errorCode)
                .build()
                .toUriString();

        log.warn("[OAuth2 FAIL] errorCode={}, failUrl={}", errorCode, failUrl);
        response.sendRedirect(failUrl);
    }

    private String extractEmail(SocialType socialType, Map<String, Object> attributes) {
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

    private String extractSocialId(SocialType socialType, Map<String, Object> attributes) {
        if (socialType == SocialType.GOOGLE) {
            Object sub = attributes.get("sub");
            if (sub != null) return String.valueOf(sub);

            // fallback
            Object id = attributes.get("id");
            return id != null ? String.valueOf(id) : null;
        }

        if (socialType == SocialType.KAKAO) {
            Object id = attributes.get("id");
            return id != null ? String.valueOf(id) : null;
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    private void logKakaoAccountStatus(Map<String, Object> attributes) {
        Object kakaoAccountObj = attributes.get("kakao_account");
        if (!(kakaoAccountObj instanceof Map<?, ?> kakaoAccount)) {
            log.warn("[KAKAO] kakao_account missing. attributes={}", attributes);
            return;
        }

        Object hasEmail = kakaoAccount.get("has_email");
        Object emailNeedsAgreement = kakaoAccount.get("email_needs_agreement");
        Object isEmailValid = kakaoAccount.get("is_email_valid");
        Object isEmailVerified = kakaoAccount.get("is_email_verified");

        log.warn("[KAKAO] has_email={}, email_needs_agreement={}, is_email_valid={}, is_email_verified={}",
                hasEmail, emailNeedsAgreement, isEmailValid, isEmailVerified);
    }
}
