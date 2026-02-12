package com.eatsfine.eatsfine.domain.user.exception.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
@Component
public class CustomOAuth2FailureHandler implements AuthenticationFailureHandler {

    private static final Logger log = LoggerFactory.getLogger(CustomOAuth2FailureHandler.class);

    private static final String ERROR_REDIRECT_BASE = "https://www.eatsfine.co.kr/login/error";

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {

        log.warn("[OAuth2 FAILURE] uri={}, msg={}", request.getRequestURI(), exception.getMessage(), exception);

        String redirectUrl = UriComponentsBuilder
                .fromUriString(ERROR_REDIRECT_BASE)
                .queryParam("error", "oauth2_login_failed")
                .build()
                .toUriString();

        response.sendRedirect(redirectUrl);
    }
}
