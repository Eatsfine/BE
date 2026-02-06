package com.eatsfine.eatsfine.domain.user.service.authService;

import com.eatsfine.eatsfine.domain.user.entity.User;
import com.eatsfine.eatsfine.domain.user.enums.Role;
import com.eatsfine.eatsfine.domain.user.exception.AuthException;
import com.eatsfine.eatsfine.domain.user.repository.UserRepository;
import com.eatsfine.eatsfine.domain.user.status.AuthErrorStatus;

import com.eatsfine.eatsfine.global.config.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthTokenServiceImpl implements AuthTokenService {

    private static final Logger log = LoggerFactory.getLogger(AuthTokenServiceImpl.class);

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    @Override
    public ReissueResult reissue(String refreshToken, Role role) {

        log.info("[REISSUE] 재발급 요청 시작");
        log.info("[REISSUE] 요청 refreshToken={}", refreshToken);

        if (refreshToken == null || refreshToken.isBlank()) {
            log.error("[REISSUE] refreshToken이 null 또는 빈 값");
            throw new AuthException(AuthErrorStatus.INVALID_TOKEN);
        }

        log.info("[REISSUE] 토큰 검증 시작");
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            log.error("[REISSUE] 토큰 검증 실패 - 만료되었거나 유효하지 않은 토큰");
            throw new AuthException(AuthErrorStatus.INVALID_TOKEN);
        }
        log.info("[REISSUE] 토큰 검증 성공");

        String email = jwtTokenProvider.getEmailFromToken(refreshToken);
        log.info("[REISSUE] 토큰에서 추출한 email={}", email);

        if (email == null || email.isBlank()) {
            log.error("[REISSUE] 토큰에서 email 추출 실패");
            throw new AuthException(AuthErrorStatus.INVALID_TOKEN);
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("[REISSUE] 사용자를 찾을 수 없음. email={}", email);
                    return new AuthException(AuthErrorStatus.INVALID_TOKEN);
                });

        log.info("[REISSUE] 사용자 조회 성공. userId={}", user.getId());
        log.info("[REISSUE] DB에 저장된 refreshToken={}", user.getRefreshToken());
        log.info("[REISSUE] 요청받은 refreshToken={}", refreshToken);
        log.info("[REISSUE] 토큰 일치 여부={}",
                user.getRefreshToken() != null && user.getRefreshToken().equals(refreshToken));

        // DB에 저장된 refreshToken과 쿠키 refreshToken이 같아야만 재발급 허용
        if (user.getRefreshToken() == null || !user.getRefreshToken().equals(refreshToken)) {
            log.error("[REISSUE] DB 토큰과 요청 토큰 불일치");
            log.error("[REISSUE] DB 토큰 null 여부={}", user.getRefreshToken() == null);
            if (user.getRefreshToken() != null) {
                log.error("[REISSUE] DB 토큰 길이={}, 요청 토큰 길이={}",
                        user.getRefreshToken().length(), refreshToken.length());
            }
            throw new AuthException(AuthErrorStatus.INVALID_TOKEN);
        }

        log.info("[REISSUE] 토큰 일치 확인 완료. 새 토큰 발급 시작");

        // 새 토큰 발급
        String newAccessToken = jwtTokenProvider.createAccessToken(email, role.name());
        String newRefreshToken = jwtTokenProvider.createRefreshToken(email);

        log.info("[REISSUE] 새 accessToken 발급 완료");
        log.info("[REISSUE] 새 refreshToken 발급 완료. 새 토큰={}", newRefreshToken);

        user.updateRefreshToken(newRefreshToken);

        log.info("[REISSUE] DB 업데이트 완료. 재발급 성공");

        return new ReissueResult(newAccessToken, newRefreshToken);
    }
}