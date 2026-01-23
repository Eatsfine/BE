package com.eatsfine.eatsfine.domain.table_layout.dto.req;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

public class TableLayoutReqDto {
    @Builder
    public record LayoutCreateDto(
            @NotNull(message = "Column 크기는 필수입니다.")
            @Min(value = 1, message = "가로 크기는 최소 1이어야 합니다.")
            @Max(value = 10, message = "가로 크기는 최대 10이어야 합니다.")
            Integer gridCol,

            @NotNull(message = "Row 크기는 필수입니다.")
            @Min(value = 1, message = "세로 크기는 최소 1이어야 합니다.")
            @Max(value = 10, message = "세로 크기는 최대 10이어야 합니다.")
            Integer gridRow
    ) {}
}
