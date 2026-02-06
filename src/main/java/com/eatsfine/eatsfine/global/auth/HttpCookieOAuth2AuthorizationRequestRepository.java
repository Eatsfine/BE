package com.eatsfine.eatsfine.global.auth;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.util.SerializationUtils;
import org.springframework.web.util.WebUtils;

import java.util.Base64;

public class HttpCookieOAuth2AuthorizationRequestRepository
        implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

    private static final Logger log = LoggerFactory.getLogger(HttpCookieOAuth2AuthorizationRequestRepository.class);

    public static final String OAUTH2_AUTH_REQUEST_COOKIE_NAME = "oauth2_auth_request";
    private static final int COOKIE_EXPIRE_SECONDS = 180;

    @Override
    public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
        log.info("[LOAD] 쿠키에서 AuthorizationRequest 로드 시도");

        //  모든 쿠키 이름 출력
        if (request.getCookies() != null) {
            for (Cookie c : request.getCookies()) {
                log.info("[LOAD] 발견된 쿠키: name={}, value={}, path={}, domain={}",
                        c.getName(),
                        c.getValue().length() > 20 ? c.getValue().substring(0, 20) + "..." : c.getValue(),
                        c.getPath(),
                        c.getDomain());
            }
        }

        Cookie cookie = WebUtils.getCookie(request, OAUTH2_AUTH_REQUEST_COOKIE_NAME);

        if (cookie == null) {
            log.warn("[LOAD] '{}' 이름의 쿠키를 찾을 수 없음!", OAUTH2_AUTH_REQUEST_COOKIE_NAME);
            return null;
        }

        if (cookie.getValue() == null || cookie.getValue().isBlank()) {
            log.warn("[LOAD] 쿠키 값이 비어있음");
            return null;
        }

        try {
            byte[] bytes = Base64.getUrlDecoder().decode(cookie.getValue());
            Object deserialized = SerializationUtils.deserialize(bytes);
            log.info("[LOAD] AuthorizationRequest 로드 성공");
            return (deserialized instanceof OAuth2AuthorizationRequest req) ? req : null;
        } catch (Exception e) {
            log.error("[LOAD] 역직렬화 실패", e);
            return null;
        }
    }

    @Override
    public void saveAuthorizationRequest(OAuth2AuthorizationRequest authorizationRequest,
                                         HttpServletRequest request,
                                         HttpServletResponse response) {
        if (authorizationRequest == null) {
            log.info("[SAVE] authorizationRequest가 null이므로 쿠키 제거");
            removeAuthorizationRequestCookies(response);
            return;
        }

        try {
            byte[] bytes = SerializationUtils.serialize(authorizationRequest);
            String value = Base64.getUrlEncoder().encodeToString(bytes);

            Cookie cookie = new Cookie(OAUTH2_AUTH_REQUEST_COOKIE_NAME, value);
            cookie.setPath("/");
            cookie.setHttpOnly(true);
            cookie.setMaxAge(COOKIE_EXPIRE_SECONDS);

            // 환경에 따라 설정
            cookie.setSecure(true);  // 로컬 HTTP: false, 운영 HTTPS: true
            cookie.setAttribute("SameSite", "Lax");
            response.addCookie(cookie);

            log.info("[SAVE] AuthorizationRequest 쿠키 저장 완료 - name={}, path={}, maxAge={}, secure={}, domain={}",
                    cookie.getName(), cookie.getPath(), cookie.getMaxAge(), cookie.getSecure(), cookie.getDomain());

        } catch (Exception e) {
            log.error("[SAVE] 직렬화 실패", e);
        }
    }

    @Override
    public OAuth2AuthorizationRequest removeAuthorizationRequest(HttpServletRequest request,
                                                                 HttpServletResponse response) {
        log.info("[REMOVE] AuthorizationRequest 제거");
        OAuth2AuthorizationRequest authRequest = loadAuthorizationRequest(request);
        removeAuthorizationRequestCookies(response);
        return authRequest;
    }

    private void removeAuthorizationRequestCookies(HttpServletResponse response) {
        Cookie cookie = new Cookie(OAUTH2_AUTH_REQUEST_COOKIE_NAME, "");
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(0);
        cookie.setSecure(true);  // 저장할 때와 동일하게
        cookie.setAttribute("SameSite", "Lax");  // 저장할 때와 동일하게
        response.addCookie(cookie);

        log.info("[REMOVE] 쿠키 제거 완료");
    }
}