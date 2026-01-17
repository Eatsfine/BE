package com.eatsfine.eatsfine.domain.businesshours.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
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
            DayOfWeek day,

            @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
            LocalTime openTime,

            @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
            LocalTime closeTime,

            boolean isClosed
    ){}

    @Builder
    public record UpdateBusinessHoursDto(
            @Valid
            List<Summary> businessHours
    ){}
}
