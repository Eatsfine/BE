package com.eatsfine.eatsfine.domain.user.converter;

import com.eatsfine.eatsfine.domain.user.dto.request.UserRequestDto;
import com.eatsfine.eatsfine.domain.user.dto.response.UserResponseDto;
import com.eatsfine.eatsfine.domain.user.entity.User;
import com.eatsfine.eatsfine.domain.user.enums.SocialType;

import java.time.LocalDateTime;

import static com.eatsfine.eatsfine.domain.user.enums.Role.ROLE_CUSTOMER;

public class UserConverter {

    public static UserResponseDto.JoinResultDto toJoinResult(User user) {
        return UserResponseDto.JoinResultDto.builder()
                .id(user.getId())
                .createdAt(user.getCreatedAt())
                .build();
    }


     //로그인 응답 변환
    public static UserResponseDto.LoginResponseDto toLoginResponse(User user, String accessToken) {
        return UserResponseDto.LoginResponseDto.builder()
                .id(user.getId())
                .accessToken(accessToken)
                .refreshToken(null)
                .build();
    }


    // 유저 정보 조회 응답 변환
    public static UserResponseDto.UserInfoDto toUserInfo(User user) {
        return UserResponseDto.UserInfoDto.builder()
                .id(user.getId())
                .profileImage(user.getProfileImage())
                .email(user.getEmail())
                .nickName(user.getNickName())
                .phoneNumber(user.getPhoneNumber())
                .build();
    }


    //유저 정보 수정 응답 변환
    public static UserResponseDto.UpdateResponseDto toUpdateResponse(User user) {
        return UserResponseDto.UpdateResponseDto.builder()
                .profileImage(user.getProfileImage())
                .email(user.getEmail())
                .nickName(user.getNickName())
                .phoneNumber(user.getPhoneNumber())
                .build();
    }


    // 비밀번호 변경 응답 변환
    public static UserResponseDto.UpdatePasswordDto toUpdatePasswordResponse(boolean changed, LocalDateTime changedAt, String message) {
        return UserResponseDto.UpdatePasswordDto.builder()
                .changed(changed)
                .changedAt(changedAt)
                .message(message)
                .build();
    }


    public static User toUser(UserRequestDto.JoinDto dto, String encodedPassword) {
        return User.builder()
                .nickName(dto.getNickName())
                .email(dto.getEmail())
                .phoneNumber(dto.getPhoneNumber())
                .password(encodedPassword)
                .role(ROLE_CUSTOMER)               // 기본 권한
                .build();
    }


    /*
     소셜 유저 생성 (최초 소셜 가입 등)
     -소셜 로그인에서 email/nickname/phoneNumber 등을 확보한 후 엔티티 생성에 사용
     */
    public static User toSocialUser(String email, String nickName, String phoneNumber, String socialId, SocialType socialType) {

        return User.builder()
                .email(email)
                .nickName(nickName)
                .phoneNumber(phoneNumber)
                .socialId(socialId)
                .socialType(socialType)
                .role(ROLE_CUSTOMER)
                .build();
    }

}
