package com.eatsfine.eatsfine.domain.tableblock.controller;

import com.eatsfine.eatsfine.domain.tableblock.dto.req.TableBlockReqDto;
import com.eatsfine.eatsfine.domain.tableblock.dto.res.TableBlockResDto;
import com.eatsfine.eatsfine.domain.tableblock.exception.status.TableBlockSuccessStatus;
import com.eatsfine.eatsfine.domain.tableblock.service.TableBlockCommandService;
import com.eatsfine.eatsfine.global.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "TableBlock", description = "테이블 슬롯 차단/해제 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class TableBlockController implements TableBlockControllerDocs {
    private final TableBlockCommandService tableBlockCommandService;

    @Override
    @PatchMapping("/stores/{storeId}/tables/{tableId}/slots")
    public ApiResponse<TableBlockResDto.SlotStatusUpdateDto> updateSlotStatus(
            @PathVariable Long storeId,
            @PathVariable Long tableId,
            @RequestBody TableBlockReqDto.SlotStatusUpdateDto dto
    ) {
        return ApiResponse.of(TableBlockSuccessStatus._SLOT_STATUS_UPDATED, tableBlockCommandService.updateSlotStatus(storeId, tableId, dto));
    }
}
