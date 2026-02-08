package com.eatsfine.eatsfine.domain.user.service.userService;


import com.eatsfine.eatsfine.domain.businessnumber.validator.BusinessNumberValidator;
import com.eatsfine.eatsfine.domain.image.exception.ImageException;
import com.eatsfine.eatsfine.domain.image.status.ImageErrorStatus;
import com.eatsfine.eatsfine.domain.term.repository.TermRepository;
import com.eatsfine.eatsfine.domain.user.converter.UserConverter;
import com.eatsfine.eatsfine.domain.user.dto.request.UserRequestDto;
import com.eatsfine.eatsfine.domain.user.dto.response.UserResponseDto;
import com.eatsfine.eatsfine.domain.user.entity.User;
import com.eatsfine.eatsfine.domain.user.enums.Role;
import com.eatsfine.eatsfine.domain.user.exception.AuthException;
import com.eatsfine.eatsfine.domain.user.exception.UserException;
import com.eatsfine.eatsfine.domain.user.repository.UserRepository;
import com.eatsfine.eatsfine.domain.user.service.userService.UserService;
import com.eatsfine.eatsfine.domain.user.status.AuthErrorStatus;
import com.eatsfine.eatsfine.domain.user.status.UserErrorStatus;
import com.eatsfine.eatsfine.global.apiPayload.exception.GeneralException;
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
import java.time.LocalDateTime;


@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final TermRepository termRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final S3Service s3Service;
    private final BusinessNumberValidator businessNumberValidator;

    @Override
    @Transactional
    public UserResponseDto.JoinResultDto signup(UserRequestDto.JoinDto joinDto) {
        // 1) 이메일 중복 체크
        if (userRepository.existsByEmail(joinDto.getEmail())) {
            throw new UserException(UserErrorStatus.EMAIL_ALREADY_EXISTS);
        }

        // 2) 비밀번호 인코딩 후 유저 생성
        String encoded = passwordEncoder.encode(joinDto.getPassword());
        User user = UserConverter.toUser(joinDto, encoded);
        User savedUser = userRepository.save(user);

        // 3) 약관 동의 내역 저장
        termRepository.save(UserConverter.toUserTerm(joinDto, savedUser));

        // 4) 응답 반환
        return UserConverter.toJoinResult(savedUser);
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
        String accessToken = jwtTokenProvider.createAccessToken(user.getEmail(),  user.getRole().name());
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

        // 이름/전화번호 부분 수정
        if (updateDto.getName() != null && !updateDto.getName().isBlank()) {
            user.updateName(updateDto.getName());
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

            // S3에 먼저 업로드
            String newKey = s3Service.upload(profileImage, directory);

            user.updateProfileImage(newKey);
            changed = true;

            // 트랜잭션 롤백 시 방금 올린 새 파일 삭제 (S3 고아 파일 방지)
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCompletion(int status) {
                    if (status == STATUS_ROLLED_BACK) {
                        try {
                            s3Service.deleteByKey(newKey);
                            log.info("트랜잭션 롤백으로 인해 업로드된 새 이미지를 삭제했습니다. key={}", newKey);
                        } catch (Exception e) {
                            log.error("롤백 후 새 이미지 삭제 실패. key={}", newKey, e);
                        }
                    }
                }
            });

            // 트랜잭션 커밋 성공 시 기존(옛날) 파일 삭제
            if (oldKey != null && !oldKey.isBlank()) {
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        try {
                            s3Service.deleteByKey(oldKey);
                            log.info("프로필 수정 완료 후 이전 이미지를 삭제했습니다. oldKey={}", oldKey);
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
                user.getName(),
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
    @Transactional
    public void withdraw(HttpServletRequest request) {
        User user = getCurrentUser(request);

        String profileImage = user.getProfileImage();
        if (profileImage != null && !profileImage.isBlank()) {
            try {
                s3Service.deleteByKey(profileImage);
            } catch (Exception e) {
                log.warn("프로필 이미지 삭제 실패. key={}", profileImage, e);
            }
        }

        user.updateRefreshToken(null);
        userRepository.delete(user);
    }

    @Override
    @Transactional
    public void logout(HttpServletRequest request) {
        User user = getCurrentUser(request);

        user.updateRefreshToken(null);
    }

    private User getCurrentUser(HttpServletRequest request) {
        String token = JwtTokenProvider.resolveToken(request);
        if (token == null || token.isBlank() || !jwtTokenProvider.validateToken(token)) {
            throw new AuthException(AuthErrorStatus.INVALID_TOKEN);
        }

        String email = jwtTokenProvider.getEmailFromToken(token);

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserException(UserErrorStatus.MEMBER_NOT_FOUND));
    }

    @Override
    @Transactional
    public UserResponseDto.VerifyOwnerDto verifyOwner(UserRequestDto.VerifyOwnerDto dto, HttpServletRequest request) {
        User user = getCurrentUser(request);
        log.info("[OwnerAuth] 사장 인증 시도 - 유저ID: {}, 이메일: {}",
                user.getId(), user.getEmail());

        if (user.getRole() == Role.ROLE_OWNER) {
            log.warn("[OwnerAuth] 인증 실패 - 이미 사장 권한을 가진 유저입니다. 유저ID: {}", user.getId());
            throw new AuthException(AuthErrorStatus.ALREADY_OWNER);
        }

        businessNumberValidator.validate(dto.getBusinessNumber(), dto.getStartDate(), user.getName());

        user.updateToOwner();
        User savedUser = userRepository.save(user);

        log.info("[OwnerAuth] 인증 성공 - 유저 권한이 OWNER로 변경되었습니다. 유저ID: {}", savedUser.getId());
        return UserConverter.toVerifyOwnerResponse(savedUser);
    }

    @Override
    @Transactional
    public UserResponseDto.UpdatePasswordDto changePassword(UserRequestDto.ChangePasswordDto requestDto, HttpServletRequest request) {

        User user = getCurrentUser(request);

        // 소셜 로그인 사용자 명시적 차단
        if (user.getPassword() == null || user.getPassword().isBlank()) {
            throw new AuthException(AuthErrorStatus.OAUTH_PASSWORD_NOT_SUPPORTED);
        }

        //  현재 비밀번호 일치 여부 확인
        if (!passwordEncoder.matches(requestDto.getCurrentPassword(), user.getPassword())) {
            throw new UserException(UserErrorStatus.PASSWORD_NOT_MATCH);
        }

        // 새 비밀번호 암호화 및 업데이트
        String encryptedPassword = passwordEncoder.encode(requestDto.getNewPassword());
        user.updatePassword(encryptedPassword);

        // 결과 반환
        return UserConverter.toUpdatePasswordResponse(true, LocalDateTime.now(), "비밀번호가 성공적으로 변경되었습니다." );
    }

}