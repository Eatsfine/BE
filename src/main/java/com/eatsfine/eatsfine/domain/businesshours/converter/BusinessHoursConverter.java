package com.eatsfine.eatsfine.domain.businesshours.converter;

import com.eatsfine.eatsfine.domain.businesshours.dto.BusinessHoursReqDto;
import com.eatsfine.eatsfine.domain.businesshours.dto.BusinessHoursResDto;
import com.eatsfine.eatsfine.domain.businesshours.entity.BusinessHours;

import java.util.List;

public class BusinessHoursConverter {

    public static BusinessHours toEntity(BusinessHoursReqDto.Summary dto) {
        return BusinessHours.builder()
                .dayOfWeek(dto.dayOfWeek())
                .openTime(dto.openTime())
                .closeTime(dto.closeTime())
                .isHoliday(dto.isClosed()) // 특정 요일 고정 휴무
                .build();
    }



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

    public static BusinessHoursResDto.UpdateBusinessHoursDto toUpdateBusinessHoursDto(Long storeId, List<String> updatedDays) {
        return BusinessHoursResDto.UpdateBusinessHoursDto.builder()
                .storeId(storeId)
                .updatedDays(updatedDays)
                .build();
    }
}
