package com.eatsfine.eatsfine.domain.storetable.dto.res;

import com.eatsfine.eatsfine.domain.storetable.enums.SeatsType;
import com.eatsfine.eatsfine.domain.tableblock.enums.SlotStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

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

    @Builder
    public record SlotListDto(
            int totalSlotCount,
            int availableSlotCount,
            List<SlotDetailDto> slots
    ) {}

    @Builder
    public record SlotDetailDto(
            @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
            LocalTime time,
            SlotStatus status,
            boolean isAvailable
    ) {}

    @Builder
    public record TableDetailDto(
            Long tableId,
            Integer minSeatCount,
            Integer maxSeatCount,
            String tableImageUrl,
            BigDecimal rating,
            Integer reviewCount,
            SeatsType seatsType,
            ReservationStatusDto reservationStatus
    ) {}

    @Builder
    public record ReservationStatusDto(
            @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
            LocalDate targetDate,
            Integer totalSlotCount,
            Integer availableSlotCount
    ) {}
}
