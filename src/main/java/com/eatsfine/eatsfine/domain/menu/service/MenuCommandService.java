package com.eatsfine.eatsfine.domain.menu.service;

import com.eatsfine.eatsfine.domain.menu.dto.MenuReqDto;
import com.eatsfine.eatsfine.domain.menu.dto.MenuResDto;
import org.springframework.web.multipart.MultipartFile;

public interface MenuCommandService {
    MenuResDto.ImageUploadDto uploadImage(Long storeId, MultipartFile file, String email);
    MenuResDto.ImageDeleteDto deleteMenuImage(Long storeId, Long menuId, String email);
    MenuResDto.MenuCreateDto createMenus(Long storeId, MenuReqDto.MenuCreateDto menuCreateDto, String email);
    MenuResDto.MenuDeleteDto deleteMenus(Long storeId, MenuReqDto.MenuDeleteDto menuDeleteDto, String email);
    MenuResDto.MenuUpdateDto updateMenu(Long storeId, Long menuId, MenuReqDto.MenuUpdateDto menuUpdateDto, String email);
    MenuResDto.SoldOutUpdateDto updateSoldOutStatus(Long storeId, Long menuId, boolean isSoldOut, String email);

}
