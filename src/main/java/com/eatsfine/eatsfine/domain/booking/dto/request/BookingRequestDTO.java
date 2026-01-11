package com.eatsfine.eatsfine.domain.booking.dto.request;

import java.time.LocalDate;
import java.time.LocalTime;

public class BookingRequestDTO {

    public record GetAvailableTimeDTO(
            Long storeId,
            LocalDate date,
            Integer partySize
    ){}

    public record GetAvailableTableDTO(
            Long storeId,
            LocalDate date,
            LocalTime time,
            Integer partySize
    ){}

}
