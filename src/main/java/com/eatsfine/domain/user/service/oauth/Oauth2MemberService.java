package com.eatsfine.domain.user.service.oauth;

import com.eatsfine.domain.user.entity.User;
import com.eatsfine.domain.user.enums.SocialType;

public interface Oauth2MemberService {
    User findOrCreateOauthUser(SocialType socialType, String socialId, String email, String nickName);
}
