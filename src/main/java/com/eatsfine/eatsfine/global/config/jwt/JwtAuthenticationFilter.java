package com.eatsfine.eatsfine.global.config.jwt;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {

        String uri = request.getRequestURI();
        log.debug("요청 URI: {}", uri);

        // 인증 없이 통과시킬 경로들
        if (uri.startsWith("/api/auth/login") ||
                uri.startsWith("/api/auth/signup") ||
                uri.startsWith("/api/auth/reissue") ||
                uri.startsWith("/oauth2") ||
                uri.startsWith("/login")) {
            chain.doFilter(request, response);
            return;
        }

        String token = JwtTokenProvider.resolveToken(request);

        if (token != null && jwtTokenProvider.validateToken(token)) {
            try {
                // uri.startsWith() 로직 삭제
                // 토큰이 없으면 아래 if문에서 알아서 걸러지고 다음 필터로 넘어감.
                // -> SecurityConfig의 permitAll() 설정에 따라 통과 여부 결정

                Authentication authentication = jwtTokenProvider.getAuthentication(token);

                if(authentication instanceof UsernamePasswordAuthenticationToken) {
                    ((UsernamePasswordAuthenticationToken) authentication)
                            .setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                }

                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (ExpiredJwtException e) {
                // 예외 로그 출력
                log.warn("만료된 JWT 토큰입니다. {}", e.getMessage());
            } catch (JwtException | IllegalArgumentException e) {
                // JWT 관련 구조적 문제는 스텍트레이스 포함해서 기록
                log.warn("유효하지 않은 JWT 토큰입니다. {}", e.getMessage(), e);
            } catch (Exception e) {
                // 예상치 못한 시스템 에러는 error 레벨로 전체 기록
                log.error("JWT 인증 과정에서 예상치 못한 오류가 발생했습니다. {}", e.getMessage(), e);
            }
        }

        chain.doFilter(request, response);
    }
}
