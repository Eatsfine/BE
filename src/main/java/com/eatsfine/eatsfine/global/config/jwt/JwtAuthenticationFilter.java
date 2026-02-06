package com.eatsfine.eatsfine.global.config.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {

        String uri = request.getRequestURI();
        log.debug("요청 URI: " + uri);

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
            } catch (Exception e) {
                // 예외 로그 출력
                log.warn("JWT 인증 오류: " + e.getMessage());
            }
        }

        chain.doFilter(request, response);
    }
}
