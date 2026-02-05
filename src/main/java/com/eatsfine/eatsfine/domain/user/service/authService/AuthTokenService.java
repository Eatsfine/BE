package com.eatsfine.eatsfine.domain.user.service.authService;

public interface AuthTokenService {

    ReissueResult reissue(String refreshToken);

    record ReissueResult(String accessToken, String refreshToken) {}
}
