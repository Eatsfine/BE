package com.eatsfine.eatsfine.domain.booking.dto.request;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class BookingRequestDTO {

    public record GetAvailableTimeDTO(
            @NotNull @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
            @NotNull @Min(1) Integer partySize,
            @NotNull Boolean isSplitAccepted
    ){}

    public record GetAvailableTableDTO(
            @NotNull @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
            @Schema(type = "string", example = "18:00", description = "HH:mm 형식으로 입력하세요.")
            @NotNull @DateTimeFormat(pattern = "HH:mm") LocalTime time,
            @NotNull @Min(1) Integer partySize,
            @NotNull Boolean isSplitAccepted,
            String seatsType
    ){}

    public record CreateBookingDTO(
            @NotNull @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
            @Schema(type = "string", example = "18:00", description = "HH:mm 형식으로 입력하세요.")
            @NotNull @DateTimeFormat(pattern = "HH:mm") LocalTime time,
            @NotNull @Min(1) Integer partySize,
            @NotNull List<Long> tableIds,
            @NotNull boolean isSplitAccepted
    ){}

    public record PaymentConfirmDTO(
            @NotBlank String paymentKey, //결제 고유 키
            @NotNull Integer amount //실제 결제 금액
    ){}

}
