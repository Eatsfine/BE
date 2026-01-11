package com.eatsfine.eatsfine.domain.booking.dto.response;

import lombok.Builder;

import java.time.LocalTime;
import java.util.List;

public class BookingResponseDTO {

    @Builder
    public record TimeSlotListDTO(
            List<LocalTime> availableTimes
    ) {}

    @Builder
    public record AvailableTableListDTO(
            List<TableInfoDTO> tables
    ) {}

    @Builder
    public record TableInfoDTO(
            Long tableId,
            String tableNumber,
            Integer tableSeats,
            String tableLocationType
    ){}
}
