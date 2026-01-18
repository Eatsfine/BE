package com.eatsfine.eatsfine.domain.table_layout.dto.res;

import com.eatsfine.eatsfine.domain.storetable.enums.SeatsType;
import lombok.Builder;

import java.util.List;

public class TableLayoutResDto {
    @Builder
    public record LayoutDetailDto(
            Long layoutId,
            Integer totalTableCount,
            GridInfo gridInfo,
            List<TableInfo> tables
    ) {}

    @Builder
    public record GridInfo(
            Integer gridCol,
            Integer gridRow
    ) {}

    @Builder
    public record TableInfo(
            Long tableId,
            String tableNumber,
            SeatsType seatsType,
            Integer minSeatCount,
            Integer maxSeatCount,
            Integer reviewCount,
            Integer gridX,
            Integer gridY,
            Integer widthSpan,
            Integer heightSpan
    ) {}
}
