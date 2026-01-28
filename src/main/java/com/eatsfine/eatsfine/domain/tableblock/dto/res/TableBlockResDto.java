package com.eatsfine.eatsfine.domain.tableblock.dto.res;

import com.eatsfine.eatsfine.domain.tableblock.enums.SlotStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalTime;

public class TableBlockResDto {
    @Builder
    public record SlotStatusUpdateDto(
            Long tableBlockId,

            Long storeTableId,

            @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
            LocalDate targetDate,

            @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
            LocalTime startTime,

            @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
            LocalTime endTime,

            SlotStatus status
    ) {}
}
