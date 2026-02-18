package com.eatsfine.eatsfine.domain.booking.dto.request;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
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
            @NotNull boolean isSplitAccepted,
            @Valid @NotEmpty(message = "예약 시 메뉴 선택은 필수입니다.") List<MenuOrderDto> menuItems
    ){}

    public record MenuOrderDto(
            @NotNull Long menuId,
            @NotNull @Min(value = 1, message = "최소 1개 이상 주문해야 합니다.") Integer quantity
    ){}

    public record PaymentConfirmDTO(
            @NotBlank String paymentKey, //결제 고유 키
            @NotNull Integer amount //실제 결제 금액
    ){}

    public record CancelBookingDTO(
            @NotBlank String reason //예약 취소 사유

    ){}

}
