package com.eatsfine.eatsfine.domain.booking.dto.request;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

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

    public record CreateBookingDTO(
            Long storeId,
            LocalDate date,
            LocalTime time,
            Integer partySize,
            List<Long> tableIds
    ){}

}
