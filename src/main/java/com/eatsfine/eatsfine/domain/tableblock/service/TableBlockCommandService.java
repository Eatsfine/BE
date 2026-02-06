package com.eatsfine.eatsfine.domain.tableblock.service;

import com.eatsfine.eatsfine.domain.tableblock.dto.req.TableBlockReqDto;
import com.eatsfine.eatsfine.domain.tableblock.dto.res.TableBlockResDto;

public interface TableBlockCommandService {
    TableBlockResDto.SlotStatusUpdateDto updateSlotStatus(Long storeId, Long tableId, TableBlockReqDto.SlotStatusUpdateDto dto, String email);
}
