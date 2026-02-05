package com.eatsfine.eatsfine.global.config.jwt;

import com.eatsfine.eatsfine.domain.user.exception.UserException;
import com.eatsfine.eatsfine.domain.user.status.UserErrorStatus;
import com.eatsfine.eatsfine.global.config.properties.Constants;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import com.eatsfine.eatsfine.global.apiPayload.code.status.ErrorStatus;
import com.eatsfine.eatsfine.global.config.properties.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private final JwtProperties jwtProperties;

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    private final long accessTokenValidity = 1000L * 60 * 60; // 1시간
    private final long refreshTokenValidity = 1000L * 60 * 60 * 24 * 7; // 7일

    public String createAccessToken(String email, String role) {
        Claims claims = Jwts.claims().setSubject(email);
        claims.put("role", role);

        Date now = new Date();
        Date expiry = new Date(now.getTime() + accessTokenValidity);
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String createRefreshToken(String email) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + refreshTokenValidity);
        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);  // 유효성 검증
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public Authentication getAuthentication(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();

        String email = claims.getSubject();

        String role = claims.get("role", String.class);

        List<SimpleGrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority(role));

        User principal = new User(email, "", authorities);
        return new UsernamePasswordAuthenticationToken(principal, token, principal.getAuthorities());
    }

    public static String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(Constants.AUTH_HEADER);

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(Constants.TOKEN_PREFIX)) {
            return bearerToken.substring(Constants.TOKEN_PREFIX.length());
        }
        return null;
    }

    public Authentication extractAuthentication(HttpServletRequest request) {
        String accessToken = resolveToken(request);
        if (accessToken == null || !validateToken(accessToken)) {
            throw new UserException(UserErrorStatus.INVALID_TOKEN);
        }
        return getAuthentication(accessToken);
    }

    public String getEmailFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();

    }

}
