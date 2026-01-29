
package com.eatsfine.eatsfine.domain.menu.dto;

import com.eatsfine.eatsfine.domain.menu.enums.MenuCategory;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;

public class MenuResDto {

    @Builder
    public record ImageUploadDto(
            String imageKey, // 메뉴 등록/수정 시 서버에 다시 보낼 키
            String imageUrl  // 프론트엔드에서 즉시 미리보기를 위한 전체 URL
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
            String imageUrl
    ){}

    @Builder
    public record MenuDeleteDto(
            List<Long> deletedMenuIds
    ){}

    @Builder
    public record MenuUpdateDto(
            Long menuId,
            String name,
            String description,
            BigDecimal price,
            MenuCategory category,
            String imageUrl
    ){}

    @Builder
    public record SoldOutUpdateDto(
            Long menuId,
            boolean isSoldOut
    ){}

    @Builder
    public record MenuListDto(
            List<MenuDetailDto> menus
    ){}

    @Builder
    public record MenuDetailDto(
            Long menuId,
            String name,
            String description,
            BigDecimal price,
            MenuCategory category,
            String imageUrl,
            boolean isSoldOut
    ){}
}
