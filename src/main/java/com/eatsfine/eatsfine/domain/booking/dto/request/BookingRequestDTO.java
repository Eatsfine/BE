package com.eatsfine.eatsfine.domain.booking.dto.request;

import java.time.LocalDate;
import java.time.LocalTime;

public class BookingRequestDTO {

    public record GetAvailableTimeDTO(
            LocalDate date,
            Integer partySize,
            Boolean isSplitAccepted
    ){}

    public record GetAvailableTableDTO(
            LocalDate date,
            LocalTime time,
            Integer partySize,
            String seatsType
    ){}

}
