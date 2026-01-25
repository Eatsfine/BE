package com.eatsfine.eatsfine.domain.storetable.controller;

import com.eatsfine.eatsfine.domain.storetable.dto.req.StoreTableReqDto;
import com.eatsfine.eatsfine.domain.storetable.dto.res.StoreTableResDto;
import com.eatsfine.eatsfine.domain.storetable.exception.status.StoreTableSuccessStatus;
import com.eatsfine.eatsfine.domain.storetable.service.StoreTableCommandService;
import com.eatsfine.eatsfine.domain.storetable.service.StoreTableQueryService;
import com.eatsfine.eatsfine.global.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Tag(name = "StoreTable", description = "가게 테이블 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class StoreTableController implements StoreTableControllerDocs {
    private final StoreTableCommandService storeTableCommandService;
    private final StoreTableQueryService storeTableQueryService;

    @PostMapping("/stores/{storeId}/tables")
    public ApiResponse<StoreTableResDto.TableCreateDto> createTable(
            @PathVariable Long storeId,
            @RequestBody StoreTableReqDto.TableCreateDto dto
    ) {
        return ApiResponse.of(StoreTableSuccessStatus._TABLE_CREATED, storeTableCommandService.createTable(storeId, dto));
    }

    @GetMapping("/stores/{storeId}/tables/{tableId}/slots")
    public ApiResponse<StoreTableResDto.SlotListDto> getTableSlots(
            @PathVariable Long storeId,
            @PathVariable Long tableId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date
    ) {
        LocalDate targetDate = (date != null) ? date : LocalDate.now();
        return ApiResponse.of(StoreTableSuccessStatus._SLOT_LIST_FOUND, storeTableQueryService.getTableSlots(storeId, tableId, targetDate));
    }

    @GetMapping("/stores/{storeId}/tables/{tableId}")
    public ApiResponse<StoreTableResDto.TableDetailDto> getTableDetail(
            @PathVariable Long storeId,
            @PathVariable Long tableId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date
    ) {
        LocalDate targetDate = (date != null) ? date : LocalDate.now();
        return ApiResponse.of(StoreTableSuccessStatus._TABLE_DETAIL_FOUND, storeTableQueryService.getTableDetail(storeId, tableId, targetDate));
    }
}
