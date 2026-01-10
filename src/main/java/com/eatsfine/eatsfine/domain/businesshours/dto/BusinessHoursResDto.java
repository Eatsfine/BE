package com.eatsfine.eatsfine.domain.businesshours.dto;

import com.eatsfine.eatsfine.domain.businesshours.enums.DayOfWeek;
import lombok.Builder;

import java.time.LocalTime;

public class BusinessHoursResDto {

    @Builder
    public record Summary(
            DayOfWeek day,
            LocalTime openTime,
            LocalTime closeTime,
            Boolean closed // 영업일은 closed = null
    ){}
}
