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
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class CustomOAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthCookieProvider authCookieProvider;

    private static final String CALLBACK_REDIRECT_BASE = "https://chicchic-mu.vercel.app/oauth/callback";
    private static final String LOGIN_ERROR_REDIRECT_BASE = "https://chicchic-mu.vercel.app/login/error";

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;

        String provider = oauthToken.getAuthorizedClientRegistrationId();
        SocialType socialType = SocialType.valueOf(provider.toUpperCase());

        OAuth2User oAuth2User = oauthToken.getPrincipal();
        String socialId = extractProviderId(socialType, oAuth2User);

        if (socialId == null || socialId.isBlank()) {
            redirectFail(response, "social_id_not_found");
            return;
        }

        User user = userRepository.findBySocialTypeAndSocialId(socialType, socialId)
                .orElse(null);

        if (user == null) {
            redirectFail(response, "user_not_found_after_oauth2");
            return;
        }

        String subject = String.valueOf(user.getId());

        String accessToken = jwtTokenProvider.createAccessToken(subject);
        String refreshToken = jwtTokenProvider.createRefreshToken(subject);

        ResponseCookie refreshCookie = authCookieProvider.refreshTokenCookie(refreshToken);
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

        String redirectUrl = UriComponentsBuilder
                .fromUriString(CALLBACK_REDIRECT_BASE)
                .queryParam("accessToken", accessToken)
                .build()
                .toUriString();

        response.sendRedirect(redirectUrl);
    }

    private void redirectFail(HttpServletResponse response, String error) throws IOException {
        String failUrl = UriComponentsBuilder
                .fromUriString(LOGIN_ERROR_REDIRECT_BASE)
                .queryParam("error", error)
                .build()
                .toUriString();
        response.sendRedirect(failUrl);
    }

    @SuppressWarnings("unchecked")
    private String extractProviderId(SocialType socialType, OAuth2User oAuth2User) {
        Map<String, Object> attributes = oAuth2User.getAttributes();

        if (socialType == SocialType.GOOGLE) {
            Object sub = attributes.get("sub");
            return sub != null ? String.valueOf(sub) : null;
        }

        if (socialType == SocialType.KAKAO) {
            Object id = attributes.get("id");
            return id != null ? String.valueOf(id) : null;
        }

        return null;
    }
}