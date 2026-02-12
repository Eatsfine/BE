package com.eatsfine.eatsfine.global.config;

import com.eatsfine.eatsfine.domain.user.exception.handler.CustomOAuth2FailureHandler;
import com.eatsfine.eatsfine.domain.user.exception.handler.CustomOAuth2SuccessHandler;
import com.eatsfine.eatsfine.domain.user.service.oauthService.CustomOAuth2MemberServiceImpl;
import com.eatsfine.eatsfine.global.auth.CustomAccessDeniedHandler;
import com.eatsfine.eatsfine.global.auth.CustomAuthenticationEntryPoint;

import com.eatsfine.eatsfine.global.auth.HttpCookieOAuth2AuthorizationRequestRepository;
import com.eatsfine.eatsfine.global.config.jwt.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.time.Duration;
import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity
public class SecurityConfig {

        private final JwtAuthenticationFilter jwtAuthenticationFilter;
        private final CustomAuthenticationEntryPoint authenticationEntryPoint;
        private final CustomAccessDeniedHandler accessDeniedHandler;
        private final CustomOAuth2MemberServiceImpl customOAuth2UserService;
        private final CustomOAuth2SuccessHandler customOAuth2SuccessHandler;
        private final CustomOAuth2FailureHandler customOAuth2FailureHandler;

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

                http
                                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                                .csrf(csrf -> csrf.disable())
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                .exceptionHandling(exceptions -> exceptions
                                                .authenticationEntryPoint(authenticationEntryPoint)
                                                .accessDeniedHandler(accessDeniedHandler))
                                .authorizeHttpRequests(auth -> auth
                                                // preflight은 항상 허용
                                                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                                                .requestMatchers("/oauth2/**", "/login/oauth2/**").permitAll()
                                                // 공개 리소스 / 인증 없이
                                                .requestMatchers(
                                                                "/api/auth/**",
                                                                "/oauth2/**",
                                                                "/swagger-ui.html",
                                                                "/swagger-ui/**",
                                                                "/v3/api-docs/**",
                                                                "/api/v1/deploy/health-check",
                                                                "/swagger-resources/**",
                                                                "/api/v1/stores/*/bookings/available-times",
                                                                "/api/v1/stores/*/bookings/available-tables",
                                                                "/api/v1/inquiries"

                                                ).permitAll()

                                                .requestMatchers(HttpMethod.GET,
                                                                "/api/v1/stores/search", // 식당 검색
                                                                "/api/v1/stores/*", // 식당 상세 조회
                                                                "/api/v1/stores/*/main-image", // 식당 대표 이미지 조회
                                                                "/api/v1/stores/*/menus", // 식당 메뉴 조회
                                                                "/api/v1/stores/*/table-images" // 식당 테이블 이미지(가게 전경) 조회
                                                ).permitAll()

                                                .requestMatchers("/auth/**", "/login/**", "/signup").permitAll()
                                                .anyRequest().authenticated())

                                .oauth2Login(oauth2 -> oauth2
                                                .authorizationEndpoint(authorization -> authorization
                                                                .authorizationRequestRepository(
                                                                                cookieAuthorizationRequestRepository()))
                                                .userInfoEndpoint(userInfo -> userInfo
                                                                .userService(customOAuth2UserService))
                                                .successHandler(customOAuth2SuccessHandler)
                                                .failureHandler(customOAuth2FailureHandler))

                        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

            return http.build();
    }

        @Bean
        public HttpCookieOAuth2AuthorizationRequestRepository cookieAuthorizationRequestRepository() {
                return new HttpCookieOAuth2AuthorizationRequestRepository();
        }


        @Bean
        public CorsConfigurationSource corsConfigurationSource() {
                CorsConfiguration config = new CorsConfiguration();

                // Merged origins from both remote and local
                config.setAllowedOriginPatterns(List.of(
                                "https://www.eatsfine.co.kr",
                                "https://eatsfine.co.kr",
                                "http://localhost:3000",
                                "http://localhost:5173"));
                config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
                config.setAllowedHeaders(List.of("*"));
                config.setExposedHeaders(List.of("Authorization", "Set-Cookie"));
                config.setAllowCredentials(true);
                config.setMaxAge(Duration.ofHours(1));

                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", config);
                return source;
        }


        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }

}
