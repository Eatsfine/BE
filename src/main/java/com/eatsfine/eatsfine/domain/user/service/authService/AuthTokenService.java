package com.eatsfine.eatsfine.domain.user.service.authService;

import com.eatsfine.eatsfine.domain.user.enums.Role;

public interface AuthTokenService {

    ReissueResult reissue(String refreshToken, Role role);

    record ReissueResult(String accessToken, String refreshToken) {}
}
