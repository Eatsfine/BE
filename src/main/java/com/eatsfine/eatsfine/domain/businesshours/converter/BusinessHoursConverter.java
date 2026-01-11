package com.eatsfine.eatsfine.domain.businesshours.converter;

import com.eatsfine.eatsfine.domain.businesshours.dto.BusinessHoursResDto;
import com.eatsfine.eatsfine.domain.businesshours.entity.BusinessHours;

public class BusinessHoursConverter {

    public static BusinessHoursResDto.Summary toSummary(BusinessHours bh) {
        // 휴무일 때
        if(bh.isHoliday()) {
            return BusinessHoursResDto.Summary.builder()
                    .day(bh.getDayOfWeek())
                    .isClosed(true)
                    .build();
        }
        // 영업일일 때
        return BusinessHoursResDto.Summary.builder()
                .day(bh.getDayOfWeek())
                .openTime(bh.getOpenTime())
                .closeTime(bh.getCloseTime())
                .isClosed(false)
                .build();
    }
}
