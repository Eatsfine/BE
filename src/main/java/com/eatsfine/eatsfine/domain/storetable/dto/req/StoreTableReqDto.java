package com.eatsfine.eatsfine.domain.storetable.dto.req;

import com.eatsfine.eatsfine.domain.storetable.enums.SeatsType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public class StoreTableReqDto {
    public record TableCreateDto(
            @NotNull(message = "X 좌표는 필수입니다.")
            @Min(value = 0, message = "X 좌표는 0 이상이어야 합니다.")
            Integer gridX,

            @NotNull(message = "Y 좌표는 필수입니다.")
            @Min(value = 0, message = "Y 좌표는 0 이상이어야 합니다.")
            Integer gridY,

            @NotNull(message = "최소 인원은 필수입니다.")
            @Min(value = 1, message = "최소 인원은 1명 이상이어야 합니다.")
            @Max(value = 20, message = "최소 인원은 20명 이하여야 합니다.")
            Integer minSeatCount,

            @NotNull(message = "최대 인원은 필수입니다.")
            @Min(value = 1, message = "최대 인원은 1명 이상이어야 합니다.")
            @Max(value = 20, message = "최대 인원은 20명 이하여야 합니다.")
            Integer maxSeatCount,

            @NotNull(message = "테이블 유형은 필수입니다.")
            SeatsType seatsType,

            String tableImageUrl
    ) {}

    public record TableUpdateDto(
            @Pattern(regexp = "^[1-9]\\d*$", message = "테이블 번호는 1 이상의 숫자여야 합니다.")
            String tableNumber,

            @Min(value = 1, message = "최소 인원은 1명 이상이어야 합니다.")
            @Max(value = 20, message = "최소 인원은 20명 이하여야 합니다.")
            Integer minSeatCount,

            @Min(value = 1, message = "최대 인원은 1명 이상이어야 합니다.")
            @Max(value = 20, message = "최대 인원은 20명 이하여야 합니다.")
            Integer maxSeatCount,

            SeatsType seatsType
    ) {
        // 최소 하나의 필드는 있어야 함
        public boolean hasAnyUpdate() {
            return tableNumber != null || minSeatCount != null
                    || maxSeatCount != null || seatsType != null;
        }
    }
}
