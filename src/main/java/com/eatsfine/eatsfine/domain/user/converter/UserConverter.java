package com.eatsfine.eatsfine.domain.user.converter;

import com.eatsfine.eatsfine.domain.user.dto.response.UserResponseDto;
import com.eatsfine.eatsfine.domain.user.entity.User;

public class UserConverter {

    public static UserResponseDto.UserInfoDto toUserInfo(User user) {
        return UserResponseDto.UserInfoDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .nickName(user.getNickName())
                .phoneNumber(user.getPhoneNumber())
                .profileImage(user.getProfileImage())
                .build();
    }

}
