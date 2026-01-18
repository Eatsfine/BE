package com.eatsfine.eatsfine.domain.table_layout.controller;

import com.eatsfine.eatsfine.domain.table_layout.dto.req.TableLayoutReqDto;
import com.eatsfine.eatsfine.domain.table_layout.dto.res.TableLayoutResDto;
import com.eatsfine.eatsfine.global.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;

public interface TableLayoutControllerDocs {
    @Operation(
            summary = "테이블 배치도 생성",
            description = """
                    사장 회원이 가게의 테이블 배치도를 생성합니다.

                    - 그리드 크기는 1x1 ~ 10x10 범위 내에서 설정 가능합니다.
                    - 가게당 활성 배치도는 1개만 존재하며, 새 배치도 생성 시 기존 배치도는 자동으로 비활성화됩니다.
                    - 생성된 배치도는 빈 상태로 생성되며, 이후 테이블 추가 API를 통해 테이블을 배치할 수 있습니다.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "배치도 생성 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 요청 (그리드 크기 범위 초과 등)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "가게를 찾을 수 없음")
    })
    ApiResponse<TableLayoutResDto.LayoutDetailDto> createLayout(
            @Parameter(description = "가게 ID", required = true, example = "1")
            Long storeId,
            @RequestBody @Valid TableLayoutReqDto.LayoutCreateDto dto
    );
}
