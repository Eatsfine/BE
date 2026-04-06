package com.eatsfine.domain.user.service.auth;

import com.eatsfine.domain.user.enums.Role;

public interface AuthTokenService {

    ReissueResult reissue(String refreshToken, Role role);

    record ReissueResult(String accessToken, String refreshToken) {}
}
