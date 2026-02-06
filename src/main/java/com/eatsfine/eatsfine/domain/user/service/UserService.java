package com.eatsfine.eatsfine.domain.user.service;

import com.eatsfine.eatsfine.domain.user.dto.request.UserRequestDto;
import com.eatsfine.eatsfine.domain.user.dto.response.UserResponseDto;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {
    UserResponseDto.JoinResultDto signup(UserRequestDto.JoinDto joinDto);

    UserResponseDto.LoginResponseDto login(UserRequestDto.LoginDto loginDto);

    @Transactional(readOnly = true)
    UserResponseDto.UserInfoDto getMemberInfo(HttpServletRequest request);

    @Transactional
    String updateMemberInfo(UserRequestDto.UpdateDto updateDto, MultipartFile profileImage, HttpServletRequest request);

    void withdraw(HttpServletRequest request);

    void logout(HttpServletRequest request);

    UserResponseDto.VerifyOwnerDto verifyOwner(UserRequestDto.VerifyOwnerDto verifyOwnerDto, HttpServletRequest request);

}
