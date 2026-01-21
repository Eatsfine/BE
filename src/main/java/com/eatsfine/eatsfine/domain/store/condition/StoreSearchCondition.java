package com.eatsfine.eatsfine.domain.store.condition;

import com.eatsfine.eatsfine.domain.store.enums.Category;
import com.eatsfine.eatsfine.domain.store.enums.StoreSortType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StoreSearchCondition {
    @Schema(description = "위도", example = "37.5665")
    @NotNull(message = "위도값은 필수입니다.")
    Double lat;

    @Schema(description = "경도", example = "127.9740")
    @NotNull(message = "경도값은 필수입니다.")
    Double lng;

    @Schema(description = "키워드", example = "강남구")
    String keyword;

    @Schema(description = "카테고리")
    Category category;

    @Schema(description = "시/도 (예: 서울특별시, 경기도, 세종특별자치시)")
    String sido;

    @Schema(description = "시/군/구 (예: 강남구, 성남시 분당구, \"\") ")
    String sigungu;

    @Schema(description = "법정동 (예: 역삼동, 서현동, 어진동)")
    String bname;

    @Schema(description = "정렬 기준", example = "DISTANCE")
    StoreSortType sort = StoreSortType.DISTANCE;
}
