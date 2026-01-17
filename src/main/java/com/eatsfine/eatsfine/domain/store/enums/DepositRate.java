package com.eatsfine.eatsfine.domain.store.enums;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Schema(
        description = "예약금 비율",
        allowableValues = {"10%", "20%", "30%", "40%", "50%"}
)
@Getter
@RequiredArgsConstructor
public enum DepositRate {
    TEN(10),
    TWENTY(20),
    THIRTY(30),
    FORTY(40),
    FIFTY(50);

    private final int percent;
}
