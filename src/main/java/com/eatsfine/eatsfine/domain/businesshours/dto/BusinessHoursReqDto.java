package com.eatsfine.eatsfine.domain.businesshours.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

public class BusinessHoursReqDto {

    @Builder
    public record Summary(

            @NotNull(message = "요일은 필수입니다.")
            @Schema(description = "요일", example = "MONDAY")
            DayOfWeek day,

            @Schema(description = "영업 시작 시간", type = "string", example = "09:00")
            @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
            LocalTime openTime,

            @Schema(description = "영업 종료 시간", type = "string", example = "22:00")
            @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
            LocalTime closeTime,

            @Schema(description = "휴무 여부", example = "false")
            boolean isClosed
    ){}

    @Builder
    public record UpdateBusinessHoursDto(
            @Valid
            List<Summary> businessHours
    ){}

    @Builder
    public record UpdateBreakTimeDto(

            @NotNull(message = "브레이크타임 시작 시간은 필수입니다.")
            @Schema(description = "브레이크 시작 시간", type = "string", example = "15:00")
            @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
            LocalTime breakStartTime,

            @NotNull(message = "브레이크타임 종료 시간은 필수입니다.")
            @Schema(description = "브레이크 시작 시간", type = "string", example = "17:00")
            @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
            LocalTime breakEndTime
    ){}
}
