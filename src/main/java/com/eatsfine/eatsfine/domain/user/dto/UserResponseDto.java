package com.eatsfine.eatsfine.domain.user.dto;

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
        private String nickName;
        private String phoneNumber;
    }

    @Getter
    @Setter
    @Builder
    public static class UpdateResponseDto{
        private String profileImage;
        private String email;
        private String nickName;
        private String phoneNumber;
    }


}
