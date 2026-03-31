package com.eatsfine.domain.tableblock.service;

import com.eatsfine.domain.tableblock.dto.request.TableBlockReqDto;
import com.eatsfine.domain.tableblock.dto.response.TableBlockResDto;

public interface TableBlockCommandService {
    TableBlockResDto.SlotStatusUpdateDto updateSlotStatus(Long storeId, Long tableId, TableBlockReqDto.SlotStatusUpdateDto dto, String email);
}
