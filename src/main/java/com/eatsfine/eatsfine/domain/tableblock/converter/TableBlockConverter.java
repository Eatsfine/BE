package com.eatsfine.eatsfine.domain.tableblock.converter;

import com.eatsfine.eatsfine.domain.tableblock.dto.res.TableBlockResDto;
import com.eatsfine.eatsfine.domain.tableblock.entity.TableBlock;
import com.eatsfine.eatsfine.domain.tableblock.enums.SlotStatus;

public class TableBlockConverter {
    public static TableBlockResDto.SlotStatusUpdateDto toSlotStatusUpdateDto(TableBlock tableBlock, SlotStatus status) {
        return TableBlockResDto.SlotStatusUpdateDto.builder()
                .tableBlockId(tableBlock.getId())
                .storeTableId(tableBlock.getStoreTable().getId())
                .targetDate(tableBlock.getTargetDate())
                .startTime(tableBlock.getStartTime())
                .endTime(tableBlock.getEndTime())
                .status(status)
                .build();
    }
}
