package com.eatsfine.eatsfine.domain.user.exception.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Component
public class CustomOAuth2FailureHandler implements AuthenticationFailureHandler {

    // TODO: 환경별로 분리 추천 (application.yml)
    private static final String ERROR_REDIRECT_BASE = "https://chicchic-mu.vercel.app/login/error";

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {

        String redirectUrl = UriComponentsBuilder
                .fromUriString(ERROR_REDIRECT_BASE)
                .queryParam("error", "oauth2_login_failed")
                .build()
                .toUriString();

        response.sendRedirect(redirectUrl);
    }
}
