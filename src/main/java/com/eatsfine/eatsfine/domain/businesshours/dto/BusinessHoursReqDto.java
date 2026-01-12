package com.eatsfine.eatsfine.domain.businesshours.dto;

import lombok.Builder;

import java.time.DayOfWeek;
import java.time.LocalTime;

public class BusinessHoursReqDto {

    @Builder
    public record Summary(
            DayOfWeek dayOfWeek,
            LocalTime openTime,
            LocalTime closeTime,
            boolean isClosed
    ){}
}
