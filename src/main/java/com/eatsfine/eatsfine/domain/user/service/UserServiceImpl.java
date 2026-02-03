package com.eatsfine.eatsfine.domain.user.service;


import com.eatsfine.eatsfine.domain.image.exception.ImageException;
import com.eatsfine.eatsfine.domain.image.status.ImageErrorStatus;
import com.eatsfine.eatsfine.domain.user.converter.UserConverter;
import com.eatsfine.eatsfine.domain.user.dto.request.UserRequestDto;
import com.eatsfine.eatsfine.domain.user.dto.response.UserResponseDto;
import com.eatsfine.eatsfine.domain.user.entity.User;
import com.eatsfine.eatsfine.domain.user.exception.UserException;
import com.eatsfine.eatsfine.domain.user.repository.UserRepository;
import com.eatsfine.eatsfine.domain.user.status.UserErrorStatus;
import com.eatsfine.eatsfine.global.config.jwt.JwtTokenProvider;
import com.eatsfine.eatsfine.global.s3.S3Service;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService{
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final S3Service s3Service;

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
    @Transactional
    public String updateMemberInfo(UserRequestDto.UpdateDto updateDto,
                                   MultipartFile profileImage,
                                   HttpServletRequest request) {

        User user = getCurrentUser(request);

        boolean changed = false;

        //닉네임/전화번호 부분 수정
        if (updateDto.getNickName() != null && !updateDto.getNickName().isBlank()) {
            user.updateNickname(updateDto.getNickName());
            changed = true;
        }
        if (updateDto.getPhoneNumber() != null && !updateDto.getPhoneNumber().isBlank()) {
            user.updatePhoneNumber(updateDto.getPhoneNumber());
            changed = true;
        }

        //프로필 이미지 부분 수정 (파일이 들어온 경우에만)
        if (profileImage != null && !profileImage.isEmpty()) {
            validateProfileImage(profileImage);

            String oldKey = user.getProfileImage();
            String directory = "users/profile/" + user.getId();
            String newKey = s3Service.upload(profileImage, directory);

            user.updateProfileImage(newKey);
            changed = true;

            // 기존 이미지가 있었으면 삭제
            if (oldKey != null && !oldKey.isBlank()) {
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        try {
                            s3Service.deleteByKey(oldKey);
                        } catch (Exception e) {
                            log.warn("이전 프로필 이미지를 삭제하는 데 실패했습니다. oldKey={}", oldKey, e);
                        }
                    }
                });
            }
        }

        if (!changed) {
            log.info("[Service] No changes detected. userId={}", user.getId());
            return "변경된 내용이 없습니다.";
        }

        userRepository.save(user);
        userRepository.flush();
        
        log.info("[Service] Updated userId={}, nickname={}, phone={}, profileKey={}",
                user.getId(),
                user.getNickName(),
                user.getPhoneNumber(),
                user.getProfileImage());

        return "회원 정보가 수정되었습니다.";
    }

    private void validateProfileImage(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new ImageException(ImageErrorStatus.INVALID_FILE_TYPE);
        }

        // 용량 제한 (5MB)
        long maxBytes = 5L * 1024 * 1024;
        if (file.getSize() > maxBytes) {
            throw new ImageException(ImageErrorStatus.FILE_TOO_LARGE);
        }
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