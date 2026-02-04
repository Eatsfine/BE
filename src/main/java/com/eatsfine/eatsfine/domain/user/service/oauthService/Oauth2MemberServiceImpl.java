package com.eatsfine.eatsfine.domain.user.service.oauthService;


import com.eatsfine.eatsfine.domain.user.converter.UserConverter;
import com.eatsfine.eatsfine.domain.user.entity.User;
import com.eatsfine.eatsfine.domain.user.enums.SocialType;
import com.eatsfine.eatsfine.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class Oauth2MemberServiceImpl implements Oauth2MemberService {

    private final UserRepository userRepository;

    @Override
    public User findOrCreateOauthUser(SocialType socialType, String socialId, String email, String nickName) {
        return userRepository.findBySocialTypeAndSocialId(socialType, socialId)
                .orElseGet(() -> createSocialUser(socialType, socialId, email, nickName));
    }
    private User createSocialUser(SocialType socialType, String socialId, String email, String nickName) {
        //소셜 로그인 시 전화번호가 없을 경우 임시 값 설정
        String defaultPhoneNumber = "000-0000-0000";

        User newUser = UserConverter.toSocialUser(email, nickName, defaultPhoneNumber, socialId, socialType);

        return userRepository.save(newUser);
    }

}
