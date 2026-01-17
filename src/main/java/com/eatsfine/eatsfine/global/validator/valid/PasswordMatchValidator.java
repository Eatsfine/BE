package com.eatsfine.eatsfine.global.validator.valid;

import com.eatsfine.eatsfine.domain.user.dto.UserRequestDto;
import com.eatsfine.eatsfine.global.validator.annotation.PasswordMatch;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordMatchValidator implements ConstraintValidator<PasswordMatch, UserRequestDto.JoinDto> {

    @Override
    public boolean isValid(UserRequestDto.JoinDto dto, ConstraintValidatorContext context) {
        if (dto.getPassword() == null || dto.getPasswordConfirm() == null) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("비밀번호와 비밀번호 확인은 필수입니다.")
                    .addPropertyNode("passwordConfirm")
                    .addConstraintViolation();
            return false;
        }

        if (!dto.getPassword().equals(dto.getPasswordConfirm())) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("비밀번호와 비밀번호 확인이 일치하지 않습니다.")
                    .addPropertyNode("passwordConfirm") // 이 필드에 오류 표시
                    .addConstraintViolation();
            return false;
        }

        return true;
    }

}
