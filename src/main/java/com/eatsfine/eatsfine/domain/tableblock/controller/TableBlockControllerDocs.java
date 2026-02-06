package com.eatsfine.eatsfine.domain.tableblock.controller;

import com.eatsfine.eatsfine.domain.tableblock.dto.req.TableBlockReqDto;
import com.eatsfine.eatsfine.domain.tableblock.dto.res.TableBlockResDto;
import com.eatsfine.eatsfine.global.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.RequestBody;

public interface TableBlockControllerDocs {
    @Operation(
            summary = "테이블 슬롯 상태 변경",
            description = """                                                                                      
                        특정 테이블의 특정 시간대를 차단하거나 해제합니다.
                        - BLOCKED: 해당 시간대를 차단합니다 (DB에 저장)
                        - AVAILABLE: 차단을 해제합니다 (DB에서 삭제)
                        차단된 시간대는 예약이 불가능하며, 슬롯 조회 시 BLOCKED 상태로 표시됩니다.
                        """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "슬롯 상태 변경 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "유효하지 않은 슬롯 상태 또는 테이블이 가게에 속하지 않음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "테이블을 찾을 수 없거나 차단 내역이 없음 (해제 시)")
    })
    ApiResponse<TableBlockResDto.SlotStatusUpdateDto> updateSlotStatus(
            @Parameter(description = "가게 ID", required = true, example = "1")
            Long storeId,

            @Parameter(description = "테이블 ID", required = true, example = "1")
            Long tableId,

            @RequestBody @Valid TableBlockReqDto.SlotStatusUpdateDto dto,

            @Parameter(hidden = true) User user

    );
}
