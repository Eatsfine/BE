package com.eatsfine.eatsfine.domain.businesshours.controller;

import com.eatsfine.eatsfine.domain.businesshours.dto.BusinessHoursReqDto;
import com.eatsfine.eatsfine.domain.businesshours.dto.BusinessHoursResDto;
import com.eatsfine.eatsfine.domain.businesshours.service.BusinessHoursCommandService;
import com.eatsfine.eatsfine.domain.businesshours.status.BusinessHoursSuccessStatus;
import com.eatsfine.eatsfine.global.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "BusinessHours", description = "영업시간 관련 API")
@RequestMapping("/api/v1")
@RestController
@RequiredArgsConstructor
public class BusinessHoursController {

    private final BusinessHoursCommandService businessHoursCommandService;

    @Operation(
            summary = "가게 영업시간 수정",
            description = "가게의 영업시간을 수정합니다."
    )
    @PatchMapping("/stores/{storeId}/business-hours")
    @PreAuthorize("hasRole('OWNER')")
    public ApiResponse<BusinessHoursResDto.UpdateBusinessHoursDto> updateBusinessHours(
            @PathVariable Long storeId,
            @RequestBody BusinessHoursReqDto.UpdateBusinessHoursDto dto
    ){
        return ApiResponse.of(
                BusinessHoursSuccessStatus._UPDATE_BUSINESS_HOURS_SUCCESS,
                businessHoursCommandService.updateBusinessHours(storeId, dto)
        );
    }

    @Operation(
            summary = "브레이크타임 설정",
            description = "가게의 브레이크타임을 설정합니다."
    )
    @PatchMapping("/stores/{storeId}/break-time")
    @PreAuthorize("hasRole('OWNER')")
    public ApiResponse<BusinessHoursResDto.UpdateBreakTimeDto> updateBreakTime(
            @PathVariable Long storeId,
            @RequestBody BusinessHoursReqDto.UpdateBreakTimeDto dto
    ){
        return ApiResponse.of(
                BusinessHoursSuccessStatus._UPDATE_BREAKTIME_SUCCESS,
                businessHoursCommandService.updateBreakTime(storeId, dto)
        );
    }
}
