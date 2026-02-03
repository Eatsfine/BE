package com.eatsfine.eatsfine.domain.user.service;


import com.eatsfine.eatsfine.domain.user.converter.UserConverter;
import com.eatsfine.eatsfine.domain.user.dto.request.UserRequestDto;
import com.eatsfine.eatsfine.domain.user.dto.response.UserResponseDto;
import com.eatsfine.eatsfine.domain.user.entity.User;
import com.eatsfine.eatsfine.domain.user.exception.UserException;
import com.eatsfine.eatsfine.domain.user.repository.UserRepository;
import com.eatsfine.eatsfine.domain.user.status.UserErrorStatus;
import com.eatsfine.eatsfine.global.config.jwt.JwtTokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService{
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public UserResponseDto.JoinResultDto signup(UserRequestDto.JoinDto joinDto) {
        // 1) 이메일 중복 체크
        if (userRepository.existsByEmail(joinDto.getEmail())) {
            throw new UserException(UserErrorStatus.EMAIL_ALREADY_EXISTS);
        }

        // 2) 비밀번호 인코딩 후 유저 생성
        String encoded = passwordEncoder.encode(joinDto.getPassword());
        User user = UserConverter.toUser(joinDto, encoded);

        // 3) 저장 및 응답
        User saved = userRepository.save(user);
        return UserConverter.toJoinResult(saved);
    }

    @Override
    @Transactional
    public UserResponseDto.LoginResponseDto login(UserRequestDto.LoginDto loginDto) {
        // 1) 사용자 조회
        User user = userRepository.findByEmail(loginDto.getEmail())
                .orElseThrow(() -> new UserException(UserErrorStatus.MEMBER_NOT_FOUND));

        // 2) 비밀번호 검증
        if (!passwordEncoder.matches(loginDto.getPassword(), user.getPassword())) {
            throw new UserException(UserErrorStatus.INVALID_PASSWORD);
        }

        // 3) 토큰 발급
        String accessToken = jwtTokenProvider.createAccessToken(user.getEmail());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getEmail());

        // 4) refreshToken 저장
        user.updateRefreshToken(refreshToken);

        return UserResponseDto.LoginResponseDto.builder()
                .id(user.getId())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    @Override
    @Transactional
    public UserResponseDto.UserInfoDto getMemberInfo(HttpServletRequest request) {
        User user = getCurrentUser(request);
        return UserConverter.toUserInfo(user);
    }

    @Override
    public String updateMemberInfo(UserRequestDto.UpdateDto updateDto,
                                   MultipartFile profileImage,
                                   HttpServletRequest request) {
        User user = getCurrentUser(request);

        if (updateDto.getNickName() != null && !updateDto.getNickName().isBlank()) {
            user.updateNickname(updateDto.getNickName());
        }
        if (updateDto.getPhoneNumber() != null && !updateDto.getPhoneNumber().isBlank()) {
            user.updatePhoneNumber(updateDto.getPhoneNumber());
        }
        if (profileImage != null && !profileImage.isEmpty()) {
            throw new UserException(UserErrorStatus.PROFILE_IMAGE_UPLOAD_NOT_SUPPORTED);
        }

        return "회원 정보가 수정되었습니다.";
    }

    @Override
    public void withdraw(HttpServletRequest request) {
        User user = getCurrentUser(request);

        user.updateRefreshToken(null);

        userRepository.delete(user);
    }

    @Override
    public void logout(HttpServletRequest request) {
        User user = getCurrentUser(request);

        user.updateRefreshToken(null);
    }

    private User getCurrentUser(HttpServletRequest request) {
        String token = JwtTokenProvider.resolveToken(request);
        if (token == null || token.isBlank() || !jwtTokenProvider.validateToken(token)) {
            throw new UserException(UserErrorStatus.INVALID_TOKEN);
        }

        String email = jwtTokenProvider.getEmailFromToken(token);

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserException(UserErrorStatus.MEMBER_NOT_FOUND));
    }
}