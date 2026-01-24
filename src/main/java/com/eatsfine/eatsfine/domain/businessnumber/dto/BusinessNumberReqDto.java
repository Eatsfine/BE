package com.eatsfine.eatsfine.domain.businessnumber.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;

public class BusinessNumberReqDto {

    @Builder
    public record BusinessNumberDto(
            @NotBlank(message = "사업자번호는 필수입니다.")
            @Pattern(regexp = "^[0-9]{10}$", message = "사업자번호는 숫자 10자리여야 합니다.")
            String businessNumber,

            @NotBlank
            @Pattern(regexp = "^[0-9]{8}$", message = "개업일자는 YYYYMMDD 형식이어야 합니다.")
            String startDate
    ){}
}
