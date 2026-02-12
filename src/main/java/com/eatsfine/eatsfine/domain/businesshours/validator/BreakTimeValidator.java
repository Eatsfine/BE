package com.eatsfine.eatsfine.domain.businesshours.validator;

import com.eatsfine.eatsfine.domain.businesshours.exception.BusinessHoursException;
import com.eatsfine.eatsfine.domain.businesshours.status.BusinessHoursErrorStatus;

import java.time.LocalTime;

public class BreakTimeValidator {

    public static void validateBreakTime(LocalTime openTime, LocalTime closeTime, LocalTime breakStartTime, LocalTime breakEndTime) {

        // 휴무일은 검증 대상이 아님
        if (openTime == null || closeTime == null || breakStartTime == null || breakEndTime == null) {
            return;
        }

        // 24시간 영업이면 모든 브레이크 타임 X
        if (openTime.equals(closeTime)) {
            throw new BusinessHoursException(BusinessHoursErrorStatus._BREAK_TIME_NOT_ALLOWED_FOR_24H);
        }

        // 1. 브레이크 시작 < 종료 검증 (자정을 넘기는 브레이크 타임은 없다고 가정)
        if (!breakEndTime.isAfter(breakStartTime)) {
            throw new BusinessHoursException(BusinessHoursErrorStatus._INVALID_BREAK_TIME);
        }

        // 2. 브레이크 타임이 영업시간 내에 있는지 검증
        if (openTime.isBefore(closeTime)) {
            // 일반 영업: open <= breakStartTime AND breakEndTime <= close 여야 함
            if (breakStartTime.isBefore(openTime) || breakEndTime.isAfter(closeTime)) {
                throw new BusinessHoursException(BusinessHoursErrorStatus._INVALID_BREAK_TIME);
            }
        } else {
            // 심야 영업: [close ~ open] 사이(영업 안 하는 시간)에 브레이크 타임이 있으면 에러
            if (!breakStartTime.isBefore(closeTime) && breakStartTime.isBefore(openTime)) {
                throw new BusinessHoursException(BusinessHoursErrorStatus._INVALID_BREAK_TIME);
            }
            if (!breakEndTime.isBefore(closeTime) && breakEndTime.isBefore(openTime)) {
                throw new BusinessHoursException(BusinessHoursErrorStatus._INVALID_BREAK_TIME);
            }
        }

    }
}

