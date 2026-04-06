package com.eatsfine.domain.tablelayout.controller;

import com.eatsfine.domain.tablelayout.dto.request.TableLayoutReqDto;
import com.eatsfine.domain.tablelayout.dto.response.TableLayoutResDto;
import com.eatsfine.domain.tablelayout.exception.status.TableLayoutSuccessStatus;
import com.eatsfine.domain.tablelayout.service.TableLayoutCommandService;
import com.eatsfine.domain.tablelayout.service.TableLayoutQueryService;
import com.eatsfine.global.annotation.CurrentUser;
import com.eatsfine.global.apipayload.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

@Tag(name = "TableLayout", description = "테이블 배치도 조회 및 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class TableLayoutController implements TableLayoutControllerDocs{
    private final TableLayoutCommandService tableLayoutCommandService;
    private final TableLayoutQueryService tableLayoutQueryService;

    @PostMapping("stores/{storeId}/layouts")
    @PreAuthorize("hasRole('OWNER')")
    public ApiResponse<TableLayoutResDto.LayoutDetailDto> createLayout(
            @PathVariable Long storeId,
            @RequestBody TableLayoutReqDto.LayoutCreateDto dto,
            @CurrentUser User user
    ) {
        return ApiResponse.of(TableLayoutSuccessStatus._LAYOUT_CREATED, tableLayoutCommandService.createLayout(storeId, dto, user.getUsername()));
    }

    @GetMapping("stores/{storeId}/layouts")
    @PreAuthorize("hasRole('OWNER')")
    public ApiResponse<TableLayoutResDto.LayoutDetailDto> getActiveLayout(
            @PathVariable Long storeId,
            @CurrentUser User user
            ) {
        TableLayoutResDto.LayoutDetailDto result = tableLayoutQueryService.getActiveLayout(storeId, user.getUsername());

        if (result == null) {
            return ApiResponse.of(TableLayoutSuccessStatus._LAYOUT_NO_CONTENT, null);
        }

        return ApiResponse.of(TableLayoutSuccessStatus._LAYOUT_FOUND, result);
    }
}
