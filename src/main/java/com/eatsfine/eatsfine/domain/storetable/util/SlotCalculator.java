package com.eatsfine.eatsfine.domain.storetable.util;

import com.eatsfine.eatsfine.domain.businesshours.entity.BusinessHours;
import com.eatsfine.eatsfine.domain.store.entity.Store;
import com.eatsfine.eatsfine.domain.storetable.entity.StoreTable;
import com.eatsfine.eatsfine.domain.storetable.exception.StoreTableException;
import com.eatsfine.eatsfine.domain.storetable.exception.status.StoreTableErrorStatus;
import com.eatsfine.eatsfine.domain.tableblock.entity.TableBlock;
import com.eatsfine.eatsfine.domain.tableblock.enums.SlotStatus;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class SlotCalculator {

    private SlotCalculator() {
        // 인스턴스화 방지
    }

    // 테이블 슬롯 계산
    public static SlotCalculationResult calculateSlots(StoreTable table, LocalDate date, List<TableBlock> tableBlocks, Set<LocalTime> bookedTimes) {
        Store store = table.getTableLayout().getStore();
        DayOfWeek dayOfWeek = date.getDayOfWeek();

        BusinessHours businessHours = store.findBusinessHoursByDay(dayOfWeek)
                .orElseThrow(() -> new StoreTableException(StoreTableErrorStatus._NO_BUSINESS_HOURS));

        // 휴무일인 경우 빈 결과 반환
        if (businessHours.isClosed()) {
            return new SlotCalculationResult(0, 0, Collections.emptyList());
        }

        // 시간 슬롯 생성
        List<TimeSlot> timeSlots = generateTimeSlots(
                businessHours.getOpenTime(),
                businessHours.getCloseTime(),
                store.getBookingIntervalMinutes()
        );

        // 차단된 시간대 추출
        Set<LocalTime> blockedTimes = extractBlockedTimes(tableBlocks);

        // 각 슬롯의 상태 결정
        List<SlotDto> slotDtoList = timeSlots.stream()
                .map(slot -> determineSlotStatus(
                        slot.time(),
                        businessHours.getBreakStartTime(),
                        businessHours.getBreakEndTime(),
                        blockedTimes,
                        bookedTimes
                ))
                .toList();

        // 통계 계산
        int totalCount = slotDtoList.size();
        int availableCount = (int) slotDtoList.stream()
                .filter(SlotDto::isAvailable)
                .count();

        return new SlotCalculationResult(totalCount, availableCount, slotDtoList);
    }

    // 영업시간 기준으로 시간 슬롯을 생성
    public static List<TimeSlot> generateTimeSlots(
            LocalTime openTime,
            LocalTime closeTime,
            int intervalMinutes
    ) {
        List<TimeSlot> slots = new ArrayList<>();
        LocalTime current = openTime;

        // 예: 22:00 종료, 30분 간격 → 21:30이 마지막
        LocalTime lastSlotTime = closeTime.minusMinutes(intervalMinutes);

        while (!current.isAfter(lastSlotTime)) {
            slots.add(new TimeSlot(current));
            current = current.plusMinutes(intervalMinutes);
        }

        return slots;
    }

    // TableBlock 엔티티 리스트에서 차단된 시간대를 추출
    public static Set<LocalTime> extractBlockedTimes(List<TableBlock> tableBlocks) {
        return tableBlocks.stream()
                .map(TableBlock::getStartTime)
                .collect(Collectors.toSet());
    }

    // 슬롯 상태 결정
    public static SlotDto determineSlotStatus(
            LocalTime slotTime,
            LocalTime breakStart,
            LocalTime breakEnd,
            Set<LocalTime> blockedTimes,
            Set<LocalTime> bookedTimes
    ) {
        // 브레이크 타임 체크
        if (breakStart != null && breakEnd != null) {
            if (!slotTime.isBefore(breakStart) && slotTime.isBefore(breakEnd)) {
                return new SlotDto(slotTime, SlotStatus.BREAK_TIME, false);
            }
        }

        // 차단 체크
        if (blockedTimes.contains(slotTime)) {
            return new SlotDto(slotTime, SlotStatus.BLOCKED, false);
        }

        // 예약 체크
        if (bookedTimes.contains(slotTime)) {
            return new SlotDto(slotTime, SlotStatus.BOOKED, false);
        }

        // 예약 가능
        return new SlotDto(slotTime, SlotStatus.AVAILABLE, true);
    }

    public record SlotCalculationResult(
            int totalSlotCount,
            int availableSlotCount,
            List<SlotDto> slots
    ) {}

    public record SlotDto(
            LocalTime time,
            SlotStatus status,
            boolean isAvailable
    ) {}

    public record TimeSlot(
            LocalTime time
    ) {}
}
