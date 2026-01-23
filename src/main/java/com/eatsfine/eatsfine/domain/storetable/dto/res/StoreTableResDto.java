package com.eatsfine.eatsfine.domain.storetable.dto.res;

import com.eatsfine.eatsfine.domain.storetable.enums.SeatsType;
import lombok.Builder;

import java.math.BigDecimal;

public class StoreTableResDto {
    @Builder
    public record TableCreateDto(
            Long tableId,
            String tableNumber,
            Integer gridX,
            Integer gridY,
            Integer widthSpan,
            Integer heightSpan,
            Integer minSeatCount,
            Integer maxSeatCount,
            SeatsType seatsType,
            BigDecimal rating,
            Integer reviewCount,
            String tableImageUrl
    ) {}
}
