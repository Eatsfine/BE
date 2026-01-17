package com.eatsfine.eatsfine.domain.businesshours.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

public class BusinessHoursResDto {

    @Builder
    public record Summary(
            DayOfWeek day,

            @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
            LocalTime openTime,

            @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
            LocalTime closeTime,

            boolean isClosed // true = 휴무, false = 영업
    ){}

    // 영업시간 수정 응답
    @Builder
    public record UpdateBusinessHoursDto(
            Long storeId,
            List<Summary> updatedBusinessHours
    ){}

    // 브레이크타임 설정 응답
    @Builder
    public record UpdateBreakTimeDto(
            Long storeId,

            @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
            LocalTime breakStartTime,

            @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
            LocalTime breakEndTime
    ){}
}
