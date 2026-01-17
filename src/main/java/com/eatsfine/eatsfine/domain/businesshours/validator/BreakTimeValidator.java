package com.eatsfine.eatsfine.domain.businesshours.validator;

import com.eatsfine.eatsfine.domain.businesshours.exception.BusinessHoursException;
import com.eatsfine.eatsfine.domain.businesshours.status.BusinessHoursErrorStatus;

import java.time.LocalTime;

public class BreakTimeValidator {

    public static void validateBreakTime(LocalTime openTime, LocalTime closeTime, LocalTime breakStartTime, LocalTime breakEndTime) {

        // 휴무일은 검증 대상이 아님
        if(openTime == null || closeTime == null) {
            return;
        }

        // start < end
        if(!breakEndTime.isAfter(breakStartTime)) {
            throw new BusinessHoursException(BusinessHoursErrorStatus._INVALID_BREAK_TIME);
        }

        // 브레이크타임이 영업시간 내에 존재
        if(breakStartTime.isBefore(openTime) || breakEndTime.isAfter(closeTime)) {
            throw new BusinessHoursException(BusinessHoursErrorStatus._BREAK_TIME_OUT_OF_BUSINESS_HOURS);
        }


    }
}
