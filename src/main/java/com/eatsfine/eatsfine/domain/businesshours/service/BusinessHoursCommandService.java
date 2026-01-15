package com.eatsfine.eatsfine.domain.businesshours.service;

import com.eatsfine.eatsfine.domain.businesshours.dto.BusinessHoursReqDto;
import com.eatsfine.eatsfine.domain.businesshours.dto.BusinessHoursResDto;

public interface BusinessHoursCommandService {
    BusinessHoursResDto.UpdateBusinessHoursDto updateBusinessHours(
            Long storeId,
            BusinessHoursReqDto.UpdateBusinessHoursDto updateBusinessHoursDto
    );
}
