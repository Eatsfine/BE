package com.eatsfine.eatsfine.domain.menu.service;

import com.eatsfine.eatsfine.domain.menu.dto.MenuReqDto;
import com.eatsfine.eatsfine.domain.menu.dto.MenuResDto;
import org.springframework.web.multipart.MultipartFile;

public interface MenuCommandService {
    MenuResDto.ImageUploadDto uploadImage(Long storeId, MultipartFile file);
    MenuResDto.ImageDeleteDto deleteMenuImage(Long storeId, Long menuId);
    MenuResDto.MenuCreateDto createMenus(Long storeId, MenuReqDto.MenuCreateDto menuCreateDto);
    MenuResDto.MenuDeleteDto deleteMenus(Long storeId, MenuReqDto.MenuDeleteDto menuDeleteDto);
    MenuResDto.MenuUpdateDto updateMenu(Long storeId, Long menuId, MenuReqDto.MenuUpdateDto menuUpdateDto);
    MenuResDto.SoldOutUpdateDto updateSoldOutStatus(Long storeId, Long menuId, boolean isSoldOut);

}
