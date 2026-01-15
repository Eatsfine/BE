package com.eatsfine.eatsfine.domain.businesshours.controller;

import com.eatsfine.eatsfine.domain.businesshours.dto.BusinessHoursReqDto;
import com.eatsfine.eatsfine.domain.businesshours.dto.BusinessHoursResDto;
import com.eatsfine.eatsfine.domain.businesshours.service.BusinessHoursCommandService;
import com.eatsfine.eatsfine.domain.businesshours.status.BusinessHoursSuccessStatus;
import com.eatsfine.eatsfine.global.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class BusinessHoursController {

    private final BusinessHoursCommandService businessHoursCommandService;

    @Operation(
            summary = "가게 영업시간 수정",
            description = "가게의 영업시간을 수정합니다."
    )
    @PatchMapping("/stores/{storeId}/business-hours")
    public ApiResponse<BusinessHoursResDto.UpdateBusinessHoursDto> updateBusinessHours(
            @PathVariable Long storeId,
            @RequestBody BusinessHoursReqDto.UpdateBusinessHoursDto dto
    ){
        return ApiResponse.of(
                BusinessHoursSuccessStatus._UPDATE_BUSINESS_HOURS_SUCCESS,
                businessHoursCommandService.updateBusinessHours(storeId, dto)
        );
    }

}
