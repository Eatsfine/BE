package com.eatsfine.eatsfine.domain.storetable.converter;

import com.eatsfine.eatsfine.domain.storetable.dto.res.StoreTableResDto;
import com.eatsfine.eatsfine.domain.storetable.entity.StoreTable;
import com.eatsfine.eatsfine.domain.storetable.util.SlotCalculator;

import java.util.List;

public class StoreTableConverter {
    // StoreTable Entity를 생성 응답 DTO로 변환
    public static StoreTableResDto.TableCreateDto toTableCreateDto(StoreTable table) {
        return StoreTableResDto.TableCreateDto.builder()
                .tableId(table.getId())
                .tableNumber(table.getTableNumber())
                .gridX(table.getGridX())
                .gridY(table.getGridY())
                .widthSpan(table.getWidthSpan())
                .heightSpan(table.getHeightSpan())
                .minSeatCount(table.getMinSeatCount())
                .maxSeatCount(table.getMaxSeatCount())
                .seatsType(table.getSeatsType())
                .rating(table.getRating())
                .reviewCount(0) // 리뷰 기능 미구현으로 0 반환
                .tableImageUrl(table.getTableImageUrl())
                .build();
    }

    public static StoreTableResDto.SlotListDto toSlotListDto(int totalCount, int availableCount, List<SlotCalculator.SlotDto> slots) {
        List<StoreTableResDto.SlotDetailDto> slotDetails = slots.stream()
                .map(slot -> StoreTableResDto.SlotDetailDto.builder()
                        .time(slot.time())
                        .status(slot.status())
                        .isAvailable(slot.isAvailable())
                        .build())
                .toList();

        return StoreTableResDto.SlotListDto.builder()
                .totalSlotCount(totalCount)
                .availableSlotCount(availableCount)
                .slots(slotDetails)
                .build();
    }
}
