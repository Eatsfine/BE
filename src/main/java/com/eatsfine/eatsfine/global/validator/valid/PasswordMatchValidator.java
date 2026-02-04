package com.eatsfine.eatsfine.global.validator.valid;


import com.eatsfine.eatsfine.global.validator.annotation.PasswordMatch;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.BeanWrapperImpl;

public class PasswordMatchValidator implements ConstraintValidator<PasswordMatch, Object> {

    private String passwordFieldName;
    private String confirmFieldName;

    @Override
    public void initialize(PasswordMatch constraintAnnotation) {
        // 어노테이션에서 정한 필드 이름을 가져옴
        this.passwordFieldName = constraintAnnotation.passwordField();
        this.confirmFieldName = constraintAnnotation.confirmField();
    }

    @Override
    public boolean isValid(Object dto, ConstraintValidatorContext context) {
        Object passwordValue = getFieldValue(dto, passwordFieldName);
        Object confirmValue = getFieldValue(dto, confirmFieldName);

        // 둘 다 null이면 검증 패스
        if (passwordValue == null || confirmValue == null) {
            return true;
        }

        // 값 비교
        if (!passwordValue.equals(confirmValue)) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                    .addPropertyNode(confirmFieldName)
                    .addConstraintViolation();
            return false;
        }

        return true;
    }

    // 필드 값을 안전하게 가져오는 헬퍼 메서드
    private Object getFieldValue(Object object, String fieldName) {
        try {
            return new BeanWrapperImpl(object).getPropertyValue(fieldName);
        } catch (Exception e) {
            return null;
        }
    }
}