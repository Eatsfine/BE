
package com.eatsfine.eatsfine.domain.menu.dto;

import com.eatsfine.eatsfine.domain.menu.enums.MenuCategory;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;

public class MenuResDto {

    @Builder
    public record ImageUploadDto(
            String imageKey
    ){}


    @Builder
    public record ImageDeleteDto(
            String deletedImageKey
    ){}

    @Builder
    public record MenuCreateDto(
            List<MenuDto> menus
    ){}

    @Builder
    public record MenuDto(
            Long menuId,
            String name,
            String description,
            BigDecimal price,
            MenuCategory category,
            String imageKey

    ){}

    @Builder
    public record MenuDeleteDto(
            List<Long> deletedMenuIds
    ){}
}
