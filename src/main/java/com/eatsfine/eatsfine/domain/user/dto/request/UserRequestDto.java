package com.eatsfine.eatsfine.domain.user.dto.request;

import com.eatsfine.eatsfine.global.validator.annotation.PasswordMatch;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

public class UserRequestDto {

    @PasswordMatch
    @Getter
    public static class JoinDto{

        @NotBlank(message = "이름은 필수입니다.")
        private String name;  // 이름

        @NotBlank(message = "이메일은 필수입니다.")
        @Email(message = "유효한 이메일 형식이어야 합니다.")
        private String email;  // 이메일

        @NotBlank(message = "휴대전화 번호는 필수입니다.")
        @Pattern(regexp = "^010\\d{8}$", message = "휴대전화 번호는 010으로 시작하는 11자리 숫자여야 합니다.")
        private String phoneNumber;  // 휴대전화 번호

        @NotBlank(message = "비밀번호는 필수 입니다.")
        @Pattern(
                regexp = "^(?=(.*[a-zA-Z].*[0-9])|(?=.*[a-zA-Z].*[!@#$%^&*])|(?=.*[0-9].*[!@#$%^&*]))[a-zA-Z0-9!@#$%^&*]{8,20}$",
                message = "비밀번호는 영문, 숫자, 특수문자 중 2가지 이상 조합이며, 8자 ~20자 이내 이어야 합니다."
        )
        private String password;

        @NotBlank(message = "비밀번호 확인은 필수입니다.")
        private String passwordConfirm;  // 비밀번호 확인

        @AssertTrue(message = "이용약관에 동의해야 합니다.")
        @Schema(description = "서비스 이용약관 동의 여부 (필수)", example = "true")
        private Boolean tosConsent;

        @AssertTrue(message = "개인정보 처리방침에 동의해야 합니다.")
        @Schema(description = "개인정보 수집 및 이용 동의 여부 (필수)", example = "true")
        private Boolean privacyConsent;

        @NotNull(message = "마케팅 정보 수신에 동의합니다")
        @Schema(description = "마케팅 정보 수신 동의 여부 (선택)", example = "false")
        private Boolean marketingConsent;

    }

    @Getter
    public static class LoginDto {
        @NotBlank(message = "이메일은 필수입니다.")
        @Email(message = "유효한 이메일 형식이어야 합니다.")
        private String email;

        @NotBlank(message = "비밀번호는 필수입니다.")
        private String password;
    }

    @Getter
    @Setter
    public static class UpdateDto {
        private String email;
        private String name;
        private String phoneNumber;
    }

    @Getter
    @PasswordMatch(passwordField = "newPassword", confirmField = "newPasswordConfirm")
    public static class ChangePasswordDto {

        @NotBlank(message = "현재 비밀번호는 필수입니다.")
        @Schema(description = "현재 비밀번호", example = "CurrentPw!123")
        private String currentPassword;

        @NotBlank(message = "새 비밀번호는 필수입니다.")
        @Pattern(
                regexp = "^(?=(.*[a-zA-Z].*[0-9])|(?=.*[a-zA-Z].*[!@#$%^&*])|(?=.*[0-9].*[!@#$%^&*]))[a-zA-Z0-9!@#$%^&*]{8,20}$",
                message = "새 비밀번호는 영문, 숫자, 특수문자 중 2가지 이상 조합이며, 8자 ~20자 이내 이어야 합니다."
        )
        @Schema(description = "새 비밀번호", example = "NewPw!1234")
        private String newPassword;

        @NotBlank(message = "새 비밀번호 확인은 필수입니다.")
        @Schema(description = "새 비밀번호 확인", example = "NewPw!1234")
        private String newPasswordConfirm;
    }

    @Getter
    @Builder
    public static class VerifyOwnerDto {

        @Schema(description = "사업자번호", example = "1234567890")
        @NotBlank(message = "사업자번호는 필수입니다.")
        @Pattern(regexp = "^[0-9]{10}$", message = "사업자번호는 숫자 10자리여야 합니다.")
        private String businessNumber;

        @Schema(description = "개업일자", example = "20240101")
        @NotBlank
        @Pattern(regexp = "^[0-9]{8}$", message = "개업일자는 YYYYMMDD 형식이어야 합니다.")
        private String startDate;
    }
}
