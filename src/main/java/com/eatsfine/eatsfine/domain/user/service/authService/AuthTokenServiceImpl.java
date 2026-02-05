package com.eatsfine.eatsfine.domain.user.service.authService;

import com.eatsfine.eatsfine.domain.user.entity.User;
import com.eatsfine.eatsfine.domain.user.exception.AuthException;
import com.eatsfine.eatsfine.domain.user.repository.UserRepository;
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
    private final UserRepository userRepository;

    @Override
    public ReissueResult reissue(String refreshToken) {

        if (refreshToken == null || refreshToken.isBlank()) {
            throw new AuthException(AuthErrorStatus.INVALID_TOKEN);
        }

        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new AuthException(AuthErrorStatus.INVALID_TOKEN);
        }

        String email = jwtTokenProvider.getEmailFromToken(refreshToken);
        if (email == null || email.isBlank()) {
            throw new AuthException(AuthErrorStatus.INVALID_TOKEN);
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AuthException(AuthErrorStatus.INVALID_TOKEN));

        // DB에 저장된 refreshToken과 쿠키 refreshToken이 같아야만 재발급 허용
        if (user.getRefreshToken() == null || !user.getRefreshToken().equals(refreshToken)) {
            throw new AuthException(AuthErrorStatus.INVALID_TOKEN);
        }

        // 새 토큰 발급
        String newAccessToken = jwtTokenProvider.createAccessToken(email);
        String newRefreshToken = jwtTokenProvider.createRefreshToken(email);

        user.updateRefreshToken(newRefreshToken);

        return new ReissueResult(newAccessToken, newRefreshToken);
    }
}