package com.eatsfine.eatsfine.domain.tableblock.validator;

import com.eatsfine.eatsfine.domain.businesshours.entity.BusinessHours;
import com.eatsfine.eatsfine.domain.store.entity.Store;
import com.eatsfine.eatsfine.domain.tableblock.exception.TableBlockException;
import com.eatsfine.eatsfine.domain.tableblock.exception.status.TableBlockErrorStatus;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

public class TableBlockValidator {
    private TableBlockValidator() {
        // 인스턴스화 방지
    }

    // 브레이크 타임 검증
    public static void validateBreakTime(Store store, LocalDate targetDate, LocalTime startTime) {
        DayOfWeek dayOfWeek = targetDate.getDayOfWeek();
        BusinessHours businessHours = store.getBusinessHoursByDay(dayOfWeek);

        boolean isBreakTime = isWithinBreakTime(businessHours.getBreakStartTime(), businessHours.getBreakEndTime(), startTime);

        if (isBreakTime) {
            throw new TableBlockException(TableBlockErrorStatus._CANNOT_UNBLOCK_BREAK_TIME);
        }
    }

    public static boolean isWithinBreakTime(LocalTime breakStart, LocalTime breakEnd, LocalTime time) {
        if (breakStart == null || breakEnd == null) {
            return false;
        }

        return !time.isBefore(breakStart) && time.isBefore(breakEnd);
    }

    // 차단 시 예약 시간대 검증
    public static void validateBlockBooking(boolean isBooked) {
        if (isBooked) {
            throw new TableBlockException(TableBlockErrorStatus._CANNOT_BLOCK_BOOKED_SLOT);
        }
    }

    // 차단 해제시 예약 시간대 검증
    public static void validateUnblockBooking(boolean isBooked) {
        if (isBooked) {
            throw new TableBlockException(TableBlockErrorStatus._CANNOT_UNBLOCK_BOOKED_SLOT);
        }
    }
}