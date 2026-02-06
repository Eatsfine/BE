package com.eatsfine.eatsfine.domain.storetable.dto.res;

import com.eatsfine.eatsfine.domain.storetable.enums.SeatsType;
import com.eatsfine.eatsfine.domain.tableblock.enums.SlotStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Builder;

import java.math.BigDecimal;
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
    public record ImageUploadDto(
            String imageKey,  // 테이블 생성/수정 시 서버에 다시 보낼 키
            String imageUrl   // 프론트엔드에서 즉시 미리보기를 위한 전체 URL
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
            boolean isAvailable,
            Long bookingId
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

    @Builder
    public record TableUpdateResultDto(
            List<UpdatedTableDto> updatedTables
    ) {}

    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record UpdatedTableDto(
            Long tableId,
            String tableNumber,
            Integer minSeatCount,
            Integer maxSeatCount,
            SeatsType seatsType
    ) {}

    @Builder
    public record TableDeleteDto(
            Long tableId
    ) {}

    @Builder
    public record UploadTableImageDto(
            Long tableId,
            String tableImageUrl
    ) {}

    @Builder
    public record DeleteTableImageDto(
            Long tableId
    ) {}
}
