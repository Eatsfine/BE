package com.eatsfine.eatsfine.domain.tableblock.dto.req;

import com.eatsfine.eatsfine.domain.tableblock.enums.SlotStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

public class TableBlockReqDto {
    public record SlotStatusUpdateDto(
            @NotNull(message = "날짜는 필수입니다.")
            @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
            LocalDate targetDate,

            @NotNull(message = "시작 시간은 필수입니다.")
            @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
            LocalTime startTime,

            @NotNull(message = "상태는 필수입니다.")
            SlotStatus status
    ) {}
}
