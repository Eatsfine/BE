package com.eatsfine.eatsfine.domain.menu.converter;

import com.eatsfine.eatsfine.domain.menu.dto.MenuResDto;
import com.eatsfine.eatsfine.domain.menu.entity.Menu;

import java.util.List;

public class MenuConverter {


    public static MenuResDto.ImageUploadDto toImageUploadDto(String imageKey){
        return MenuResDto.ImageUploadDto.builder()
                .imageKey(imageKey)
                .build();
    }

    public static MenuResDto.ImageDeleteDto toImageDeleteDto(String imageKey) {
        return MenuResDto.ImageDeleteDto.builder()
                .deletedImageKey(imageKey)
                .build();
    }

    public static MenuResDto.MenuCreateDto toCreateDto(List<Menu> menus){
        List<MenuResDto.MenuDto> menuDtos = menus.stream()
                .map(menu -> MenuResDto.MenuDto.builder()
                        .menuId(menu.getId())
                        .name(menu.getName())
                        .description(menu.getDescription())
                        .price(menu.getPrice())
                        .category(menu.getMenuCategory())
                        .imageKey(menu.getImageKey())
                        .build()
                )
                .toList();

        return MenuResDto.MenuCreateDto.builder()
                .menus(menuDtos)
                .build();
    }

    public static MenuResDto.MenuDeleteDto toDeleteDto(List<Long> menuIds){
        return MenuResDto.MenuDeleteDto.builder()
                .deletedMenuIds(menuIds)
                .build();
    }

    public static MenuResDto.MenuUpdateDto toUpdateDto(Menu menu){
        return MenuResDto.MenuUpdateDto.builder()
                .menuId(menu.getId())
                .name(menu.getName())
                .description(menu.getDescription())
                .price(menu.getPrice())
                .category(menu.getMenuCategory())
                .imageKey(menu.getImageKey())
                .build();

    }

}
