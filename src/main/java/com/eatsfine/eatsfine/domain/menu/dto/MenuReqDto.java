package com.eatsfine.eatsfine.domain.menu.dto;

import com.eatsfine.eatsfine.domain.menu.enums.MenuCategory;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.List;

public class MenuReqDto {

    public record MenuCreateDto(
            @Valid
            @NotNull
            @Size(min = 1, message = "최소 1개 이상의 메뉴를 등록해야 합니다.")
            List<MenuDto> menus
    ){}


    public record MenuDto(
            @NotBlank(message = "메뉴 이름은 필수입니다.")
            String name,

            @Size(max = 500, message = "설명은 500자 이내여야 합니다.")
            String description,

            @NotNull(message = "가격은 필수입니다.")
            @Min(value = 0, message = "가격은 0원 이상이어야 합니다.")
            BigDecimal price,

            @NotNull(message = "카테고리는 필수입니다.")
            MenuCategory category,

            String imageKey // 이미지는 선택 사항이므로 검증 없음 (nullable)
    ){}


    public record MenuDeleteDto(
            @NotNull
            @Size(min = 1, message = "삭제할 메뉴를 최소 1개 이상 선택해주세요.")
            List<Long> menuIds
    ){}

    public record MenuUpdateDto(
            @Size(min = 1, message = "메뉴 이름은 1글자 이상이어야 합니다.")
            String name,
            @Size(max = 500, message = "설명은 500자 이내여야 합니다.")
            String description,
            @Min(value = 0, message = "가격은 0원 이상이어야 합니다.")
            BigDecimal price,
            MenuCategory category,
            String imageKey
    ){}

    public record SoldOutUpdateDto(
            @NotNull
            boolean isSoldOut
    ){}
}