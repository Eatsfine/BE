package com.eatsfine.eatsfine.domain.user.service.authService;

import com.eatsfine.eatsfine.domain.user.exception.AuthException;
import com.eatsfine.eatsfine.domain.user.status.AuthErrorStatus;
import com.eatsfine.eatsfine.global.config.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthTokenServiceImpl implements AuthTokenService {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public ReissueResult reissue(String refreshToken) {

        if (refreshToken == null || refreshToken.isBlank()) {
            throw new AuthException(AuthErrorStatus.INVALID_TOKEN);
        }

        // validateToken이 만료/위조를 구분 못하면 일단 INVALID로 처리
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new AuthException(AuthErrorStatus.INVALID_TOKEN);
        }

        String subject = jwtTokenProvider.getEmailFromToken(refreshToken);
        if (subject == null || subject.isBlank()) {
            throw new AuthException(AuthErrorStatus.INVALID_TOKEN);
        }

        // 새 토큰 발급
        String newAccessToken = jwtTokenProvider.createAccessToken(subject);
        String newRefreshToken = jwtTokenProvider.createRefreshToken(subject);

        return new ReissueResult(newAccessToken, newRefreshToken);
    }
}
