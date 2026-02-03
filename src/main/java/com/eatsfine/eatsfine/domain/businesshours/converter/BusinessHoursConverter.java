package com.eatsfine.eatsfine.domain.businesshours.converter;

import com.eatsfine.eatsfine.domain.businesshours.dto.BusinessHoursReqDto;
import com.eatsfine.eatsfine.domain.businesshours.dto.BusinessHoursResDto;
import com.eatsfine.eatsfine.domain.businesshours.entity.BusinessHours;

import java.util.List;

public class BusinessHoursConverter {

    public static BusinessHours toEntity(BusinessHoursReqDto.Summary dto) {
        return BusinessHours.builder()
                .dayOfWeek(dto.day())
                .openTime(dto.openTime())
                .closeTime(dto.closeTime())
                .isClosed(dto.isClosed()) // 특정 요일 고정 휴무
                .build();
    }



    public static BusinessHoursResDto.Summary toSummary(BusinessHours bh) {
        // 휴무일 때
        if(bh.isClosed()) {
            return BusinessHoursResDto.Summary.builder()
                    .day(bh.getDayOfWeek())
                    .openTime(null)
                    .closeTime(null)
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

    public static BusinessHoursResDto.UpdateBusinessHoursDto toUpdateBusinessHoursDto(Long storeId, List<BusinessHours> updatedBusinessHours) {
        return BusinessHoursResDto.UpdateBusinessHoursDto.builder()
                .storeId(storeId)
                .updatedBusinessHours(
                        updatedBusinessHours.stream().map(
                                BusinessHoursConverter::toSummary
                        ).toList()
                )
                .build();
    }

    public static BusinessHoursResDto.UpdateBreakTimeDto toUpdateBreakTimeDto(Long storeId, BusinessHoursReqDto.UpdateBreakTimeDto dto) {
        return BusinessHoursResDto.UpdateBreakTimeDto.builder()
                .storeId(storeId)
                .breakStartTime(dto.breakStartTime())
                .breakEndTime(dto.breakEndTime())
                .build();
    }
}
