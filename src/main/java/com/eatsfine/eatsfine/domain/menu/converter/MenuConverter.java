package com.eatsfine.eatsfine.domain.menu.converter;

import com.eatsfine.eatsfine.domain.menu.dto.MenuResDto;
import com.eatsfine.eatsfine.domain.menu.entity.Menu;

import java.util.List;

public class MenuConverter {


    public static MenuResDto.ImageUploadDto toImageUploadDto(String imageKey, String imageUrl){
        return MenuResDto.ImageUploadDto.builder()
                .imageKey(imageKey)
                .imageUrl(imageUrl)
                .build();
    }

    public static MenuResDto.ImageDeleteDto toImageDeleteDto(String imageKey) {
        return MenuResDto.ImageDeleteDto.builder()
                .deletedImageKey(imageKey)
                .build();
    }


    public static MenuResDto.MenuCreateDto toCreateDto(List<MenuResDto.MenuDto> menuDtos) {
        return MenuResDto.MenuCreateDto.builder()
                .menus(menuDtos)
                .build();
    }

    public static MenuResDto.MenuDeleteDto toDeleteDto(List<Long> menuIds){
        return MenuResDto.MenuDeleteDto.builder()
                .deletedMenuIds(menuIds)
                .build();
    }

    public static MenuResDto.MenuUpdateDto toUpdateDto(Menu menu, String updatedImageUrl){
        return MenuResDto.MenuUpdateDto.builder()
                .menuId(menu.getId())
                .name(menu.getName())
                .description(menu.getDescription())
                .price(menu.getPrice())
                .category(menu.getMenuCategory())
                .imageUrl(updatedImageUrl)
                .build();

    }

    public static MenuResDto.SoldOutUpdateDto toSoldOutUpdateDto(Menu menu){
        return MenuResDto.SoldOutUpdateDto.builder()
                .menuId(menu.getId())
                .isSoldOut(menu.isSoldOut())
                .build();
    }

}
