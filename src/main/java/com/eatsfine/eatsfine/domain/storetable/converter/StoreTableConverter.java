package com.eatsfine.eatsfine.domain.storetable.converter;

import com.eatsfine.eatsfine.domain.storetable.dto.req.StoreTableReqDto;
import com.eatsfine.eatsfine.domain.storetable.dto.res.StoreTableResDto;
import com.eatsfine.eatsfine.domain.storetable.entity.StoreTable;
import com.eatsfine.eatsfine.domain.storetable.util.SlotCalculator;

import java.time.LocalDate;
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

    public static StoreTableResDto.TableDetailDto toTableDetailDto(StoreTable table, LocalDate targetDate, int totalSlotCount, int availableSlotCount) {
        return StoreTableResDto.TableDetailDto.builder()
                .tableId(table.getId())
                .minSeatCount(table.getMinSeatCount())
                .maxSeatCount(table.getMaxSeatCount())
                .tableImageUrl(table.getTableImageUrl())
                .rating(table.getRating())
                .reviewCount(0) // 리뷰 기능 미구현으로 0 반환
                .seatsType(table.getSeatsType())
                .reservationStatus(
                        StoreTableResDto.ReservationStatusDto.builder()
                                .targetDate(targetDate)
                                .totalSlotCount(totalSlotCount)
                                .availableSlotCount(availableSlotCount)
                                .build()
                )
                .build();
    }

    public static StoreTableResDto.TableUpdateResultDto toTableUpdateResultDto(List<StoreTable> updatedTables, StoreTableReqDto.TableUpdateDto requestDto) {
        List<StoreTableResDto.UpdatedTableDto> updatedTableDtoList = updatedTables.stream()
                .map(table -> {
                    var builder = StoreTableResDto.UpdatedTableDto.builder()
                            .tableId(table.getId()); // tableId는 항상 포함

                    // 요청 DTO에 있는 필드만 응답에 포함
                    if (requestDto.tableNumber() != null) {
                        builder.tableNumber(table.getTableNumber());
                    }
                    if (requestDto.minSeatCount() != null || requestDto.maxSeatCount() != null) {
                        builder.minSeatCount(table.getMinSeatCount());
                        builder.maxSeatCount(table.getMaxSeatCount());
                    }
                    if (requestDto.seatsType() != null) {
                        builder.seatsType(table.getSeatsType());
                    }

                    return builder.build();
                })
                .toList();

        return StoreTableResDto.TableUpdateResultDto.builder()
                .updatedTables(updatedTableDtoList)
                .build();
    }
}
