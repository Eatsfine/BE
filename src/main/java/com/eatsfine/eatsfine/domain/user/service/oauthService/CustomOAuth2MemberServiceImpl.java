package com.eatsfine.eatsfine.domain.user.service.oauthService;

import com.eatsfine.eatsfine.domain.user.enums.SocialType;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class CustomOAuth2MemberServiceImpl extends DefaultOAuth2UserService {

    private final Oauth2MemberService oauth2MemberService;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String provider = userRequest.getClientRegistration().getRegistrationId();
        Map<String, Object> attributes = oAuth2User.getAttributes();

        String providerId = null;
        String email = null;
        String name = null;

        SocialType socialType = SocialType.valueOf(provider.toUpperCase());

        try {
            if (socialType == SocialType.GOOGLE) {
                providerId = (String) attributes.get("sub");
                email = (String) attributes.get("email");
                name = (String) attributes.get("name");
            } else if (socialType == SocialType.KAKAO) {
                Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
                providerId = String.valueOf(attributes.get("id"));
                if (kakaoAccount != null) {
                    email = (String) kakaoAccount.get("email");
                    Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
                    if (profile != null) {
                        name = (String) profile.get("nickname");
                    }
                }
            }

            // 회원 생성/조회
            oauth2MemberService.findOrCreateOauthUser(socialType, providerId, email, name);

        } catch (Exception e) {
            OAuth2Error error = new OAuth2Error("invalid_request", e.getMessage(), null);
            throw new OAuth2AuthenticationException(error, e);
        }

        return oAuth2User;
    }
}