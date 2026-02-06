package com.eatsfine.eatsfine.domain.user.service.oauthService;


import com.eatsfine.eatsfine.domain.user.converter.UserConverter;
import com.eatsfine.eatsfine.domain.user.entity.User;
import com.eatsfine.eatsfine.domain.user.enums.SocialType;
import com.eatsfine.eatsfine.domain.user.exception.AuthException;
import com.eatsfine.eatsfine.domain.user.repository.UserRepository;
import com.eatsfine.eatsfine.domain.user.status.AuthErrorStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class Oauth2MemberServiceImpl implements Oauth2MemberService {

    private final UserRepository userRepository;

    @Override
    public User findOrCreateOauthUser(SocialType socialType, String socialId, String email, String nickName) {

        if (email == null || email.isBlank()) {
            throw new AuthException(AuthErrorStatus.OAUTH2_EMAIL_NOT_FOUND);
        }

        // 소셜 ID로 이미 가입된 회원이 있는지 조회
        return userRepository.findBySocialTypeAndSocialId(socialType, socialId)
                .orElseGet(() -> findByEmailOrJoin(socialType, socialId, email, nickName));
    }

    private User findByEmailOrJoin(SocialType socialType, String socialId, String email, String nickName) {
        // 소셜 ID는 없지만, "같은 이메일"을 쓰는 기존 회원이 있는지 조회
        Optional<User> existingUser = userRepository.findByEmail(email);

        if (existingUser.isPresent()) {
            // 이미 가입된 이메일이 있음 -> 계정 연동 (소셜 정보만 업데이트)
            User user = existingUser.get();
            log.info("기존 회원 계정 연동: email={}, socialType={}", email, socialType);

            user.linkSocial(socialType, socialId);
            return user;
        }

        // 아예 처음 온 회원 -> 신규 회원가입
        return createSocialUser(socialType, socialId, email, nickName);
    }

    private User createSocialUser(SocialType socialType, String socialId, String email, String nickName) {
        log.info("신규 소셜 회원 가입: email={}", email);

        // 전화번호에 null 전달
        User newUser = UserConverter.toSocialUser(email, nickName, null, socialId, socialType);

        return userRepository.save(newUser);
    }

}
