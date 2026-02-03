package com.eatsfine.eatsfine.domain.user.controller;


import com.eatsfine.eatsfine.domain.user.dto.request.UserRequestDto;
import com.eatsfine.eatsfine.domain.user.dto.response.UserResponseDto;
import com.eatsfine.eatsfine.domain.user.exception.UserException;
import com.eatsfine.eatsfine.domain.user.service.UserService;
import com.eatsfine.eatsfine.domain.user.status.UserErrorStatus;
import com.eatsfine.eatsfine.global.apiPayload.ApiResponse;
import com.eatsfine.eatsfine.global.auth.AuthCookieProvider;
import com.eatsfine.eatsfine.global.config.jwt.JwtTokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthCookieProvider authCookieProvider;

    @PostMapping("/api/auth/signup")
    @Operation(summary = "회원가입 API", description = "회원가입을 처리하는 API입니다.")
    public ResponseEntity<UserResponseDto.JoinResultDto> signup(@RequestBody @Valid UserRequestDto.JoinDto joinDto) {
        UserResponseDto.JoinResultDto result = userService.signup(joinDto);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/api/auth/login")
    @Operation(summary = "로그인 API", description = "사용자 로그인을 처리하는 API입니다.")
    public ResponseEntity<ApiResponse<UserResponseDto.LoginResponseDto>> login(@RequestBody UserRequestDto.LoginDto loginDto) {
        UserResponseDto.LoginResponseDto loginResult = userService.login(loginDto);

        if (loginResult.getRefreshToken() == null || loginResult.getRefreshToken().isBlank()) {
            throw new UserException(UserErrorStatus.REFRESH_TOKEN_NOT_ISSUED);
        }

        ResponseCookie refreshCookie = authCookieProvider.refreshTokenCookie(loginResult.getRefreshToken());

        UserResponseDto.LoginResponseDto body = UserResponseDto.LoginResponseDto.builder()
                .id(loginResult.getId())
                .accessToken(loginResult.getAccessToken())
                .refreshToken(null)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(ApiResponse.onSuccess(body));
    }

    @GetMapping("/api/v1/member/info")
    @Operation(
            summary = "유저 내 정보 조회 API - 인증 필요",
            description = "유저가 내 정보를 조회하는 API입니다.",
            security = {@SecurityRequirement(name = "JWT")}
    )
    public ApiResponse<UserResponseDto.UserInfoDto> getMyInfo(HttpServletRequest request) {
        return ApiResponse.onSuccess(userService.getMemberInfo(request));
    }

    @PutMapping(value = "/api/v1/member/info", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "회원 정보 수정 API - 인증 필요",
            description = "회원 정보를 수정하는 API입니다. (프로필 이미지 포함)",
            security = {@SecurityRequirement(name = "JWT")}
    )
    public ResponseEntity<ApiResponse<String>> updateMyInfo(
            @RequestPart("updateDto") @Valid UserRequestDto.UpdateDto updateDto,
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage,
            HttpServletRequest request
    ) {
        userService.updateMemberInfo(updateDto, profileImage, request);
        return ResponseEntity.ok(ApiResponse.onSuccess("회원 정보가 수정되었습니다."));
    }

    @DeleteMapping("/api/auth/withdraw")
    @Operation(
            summary = "회원 탈퇴 API - 인증 필요",
            description = "회원 탈퇴 기능 API입니다.",
            security = {@SecurityRequirement(name = "JWT")}
    )
    public ResponseEntity<?> withdraw(HttpServletRequest request) {
        userService.withdraw(request);
        return ResponseEntity.ok(ApiResponse.onSuccess("회원 탈퇴가 완료되었습니다."));
    }

    @DeleteMapping("/api/auth/logout")
    @Operation(
            summary = "회원 로그아웃 API - 인증 필요",
            description = "회원 로그아웃 기능 API입니다.",
            security = {@SecurityRequirement(name = "JWT")}
    )
    public ResponseEntity<ApiResponse<String>> logout(HttpServletRequest request) {
        userService.logout(request);
        return ResponseEntity.ok(ApiResponse.onSuccess("로그아웃이 되었습니다."));
    }

}
