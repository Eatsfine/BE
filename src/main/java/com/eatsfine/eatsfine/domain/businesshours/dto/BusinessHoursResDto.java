package com.eatsfine.eatsfine.domain.businesshours.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;

import java.time.DayOfWeek;
import java.time.LocalTime;

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
}
