package com.eatsfine.eatsfine.domain.inquiry.dto;

import com.eatsfine.eatsfine.domain.inquiry.enums.InquiryType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class InquiryRequestDTO {
    @NotBlank(message = "이름을 입력해주세요.")
    @Size(max = 20)
    private String name;

    @NotBlank(message = "이메일을 입력해주세요.")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    @Size(max = 50)
    private String email;

    @NotNull(message = "문의 유형을 선택해주세요.")
    private InquiryType type;

    @NotBlank(message = "제목을 입력해주세요.")
    @Size(max = 100)
    private String title;

    @NotBlank(message = "문의 내용을 입력해주세요.")
    @Size(max = 2000)
    private String content;
}
