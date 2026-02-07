package com.eatsfine.eatsfine.domain.user.service.oauthService;

import com.eatsfine.eatsfine.domain.user.entity.User;
import com.eatsfine.eatsfine.domain.user.enums.SocialType;

public interface Oauth2MemberService {
    User findOrCreateOauthUser(SocialType socialType, String socialId, String email, String nickName);
}
