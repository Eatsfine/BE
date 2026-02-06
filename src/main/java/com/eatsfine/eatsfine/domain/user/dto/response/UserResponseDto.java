package com.eatsfine.eatsfine.domain.user.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

public class UserResponseDto {

    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class JoinResultDto{
        private Long id;
        private LocalDateTime createdAt;
    }

    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class LoginResponseDto{
        private Long id;
        private String accessToken;
        private String refreshToken;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfoDto{
        private Long id;
        private String profileImage;
        private String email;
        private String name;
        private String phoneNumber;
    }

    @Getter
    @Setter
    @Builder
    public static class UpdateResponseDto{
        private String profileImage;
        private String email;
        private String name;
        private String phoneNumber;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class UpdatePasswordDto {

        @Schema(description = "비밀번호 변경 완료 여부", example = "true")
        private boolean changed;

        @Schema(description = "비밀번호 변경 완료 시각", example = "2026-01-30T18:25:43")
        private LocalDateTime changedAt;

        @Schema(description = "응답 메시지", example = "비밀번호가 성공적으로 변경되었습니다.")
        private String message;
    }

    @Getter
    @AllArgsConstructor
    public static class AccessTokenResponse {
        private String accessToken;
    }

    @Builder
    public static class VerifyOwnerDto {
        @Schema(description = "권한 승격이 완료된 유저의 식별자", example = "1")
        private Long userId;
    }
}