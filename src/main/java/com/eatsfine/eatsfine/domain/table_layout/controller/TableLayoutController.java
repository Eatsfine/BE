package com.eatsfine.eatsfine.domain.table_layout.controller;

import com.eatsfine.eatsfine.domain.table_layout.dto.req.TableLayoutReqDto;
import com.eatsfine.eatsfine.domain.table_layout.dto.res.TableLayoutResDto;
import com.eatsfine.eatsfine.domain.table_layout.exception.status.TableLayoutSuccessStatus;
import com.eatsfine.eatsfine.domain.table_layout.service.TableLayoutCommandService;
import com.eatsfine.eatsfine.global.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "TableLayout", description = "테이블 배치도 조회 및 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class TableLayoutController implements TableLayoutControllerDocs{
    private final TableLayoutCommandService tableLayoutCommandService;

    @PostMapping("stores/{storeId}/layouts")
    public ApiResponse<TableLayoutResDto.LayoutDetailDto> createLayout(
            @PathVariable Long storeId,
            @RequestBody TableLayoutReqDto.LayoutCreateDto dto
    ) {
        return ApiResponse.of(TableLayoutSuccessStatus._LAYOUT_CREATED, tableLayoutCommandService.createLayout(storeId, dto));
    }
}
