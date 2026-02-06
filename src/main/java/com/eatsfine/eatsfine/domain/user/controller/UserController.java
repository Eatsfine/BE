package com.eatsfine.eatsfine.domain.user.controller;


import com.eatsfine.eatsfine.domain.user.dto.request.UserRequestDto;
import com.eatsfine.eatsfine.domain.user.dto.response.UserResponseDto;
import com.eatsfine.eatsfine.domain.user.exception.UserException;
import com.eatsfine.eatsfine.domain.user.service.userService.UserService;
import com.eatsfine.eatsfine.domain.user.status.UserErrorStatus;
import com.eatsfine.eatsfine.global.apiPayload.ApiResponse;
import com.eatsfine.eatsfine.global.auth.AuthCookieProvider;
import com.eatsfine.eatsfine.global.config.jwt.JwtTokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestBody;
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


@Tag(name = "User", description = "회원 관리 API")
@Slf4j
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
    public ResponseEntity<ApiResponse<UserResponseDto.LoginResponseDto>> login(@RequestBody @Valid UserRequestDto.LoginDto loginDto) {
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


    @PatchMapping(value = "/api/v1/member/info")
    @Operation(
            summary = "닉네임/전화번호 수정 API - 인증 필요",
            description = "닉네임/전화번호만 수정합니다. (JSON)",
            security = {@SecurityRequirement(name = "JWT")}
    )
    public ResponseEntity<ApiResponse<String>> updateMyInfoText(
            @RequestBody @Valid UserRequestDto.UpdateDto updateDto, HttpServletRequest request
    ) {
        String result = userService.updateMemberInfo(updateDto, null, request);
        return ResponseEntity.ok(ApiResponse.onSuccess(result));
    }


    @PutMapping(
            value = "/api/v1/member/profile-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "프로필 이미지 수정 API - 인증 필요",
            description = "프로필 이미지만 수정합니다. (multipart/form-data)",
            security = {@SecurityRequirement(name = "JWT")}
    )
    public ResponseEntity<ApiResponse<String>> updateProfileImage(
            @RequestPart(value = "profileImage") MultipartFile profileImage,
            HttpServletRequest request
    ) {
        String result = userService.updateMemberInfo(null, profileImage, request);
        return ResponseEntity.ok(ApiResponse.onSuccess(result));
    }


    @DeleteMapping("/api/auth/withdraw")
    @Operation(
            summary = "회원 탈퇴 API - 인증 필요",
            description = "회원 탈퇴 기능 API입니다.",
            security = {@SecurityRequirement(name = "JWT")}
    )
    public ResponseEntity<?> withdraw(HttpServletRequest request) {
        userService.withdraw(request);

        //회원탈퇴 시 refreshToken 쿠키도 삭제
        ResponseCookie clearCookie = authCookieProvider.clearRefreshTokenCookie();
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, clearCookie.toString())
                .body(ApiResponse.onSuccess("회원 탈퇴가 완료되었습니다."));
    }


    @DeleteMapping("/api/auth/logout")
    @Operation(
            summary = "회원 로그아웃 API - 인증 필요",
            description = "회원 로그아웃 기능 API입니다.",
            security = {@SecurityRequirement(name = "JWT")}
    )
    public ResponseEntity<ApiResponse<String>> logout(HttpServletRequest request) {
        userService.logout(request);
        ResponseCookie clearCookie = authCookieProvider.clearRefreshTokenCookie();
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, clearCookie.toString())
                .body(ApiResponse.onSuccess("로그아웃이 되었습니다."));
    }

}
