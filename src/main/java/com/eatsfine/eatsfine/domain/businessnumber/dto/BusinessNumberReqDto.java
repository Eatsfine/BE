package com.eatsfine.eatsfine.domain.businessnumber.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;

public class BusinessNumberReqDto {

    @Builder
    public record BusinessNumberDto(
        
            @Schema(description = "이름", example = "홍길동")
            @NotBlank(message = "이름은 필수입니다.")
            @Size(min = 2, max = 20, message = "이름은 2자 이상 20자 이내여야 합니다.")
            String name,

            @NotBlank(message = "사업자번호는 필수입니다.")
            @Pattern(regexp = "^[0-9]{10}$", message = "사업자번호는 숫자 10자리여야 합니다.")
            String businessNumber,

            @NotBlank
            @Pattern(regexp = "^[0-9]{8}$", message = "개업일자는 YYYYMMDD 형식이어야 합니다.")
            String startDate
    ){}
}
