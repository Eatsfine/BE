package com.eatsfine.domain.businesshours.service;

import com.eatsfine.domain.businesshours.dto.request.BusinessHoursReqDto;
import com.eatsfine.domain.businesshours.dto.response.BusinessHoursResDto;

public interface BusinessHoursCommandService {
    BusinessHoursResDto.UpdateBusinessHoursDto updateBusinessHours(
            Long storeId,
            BusinessHoursReqDto.UpdateBusinessHoursDto updateBusinessHoursDto,
            String email
    );

    BusinessHoursResDto.UpdateBreakTimeDto updateBreakTime(
            Long storeId,
            BusinessHoursReqDto.UpdateBreakTimeDto dto,
            String email
    );

}
