package com.eatsfine.eatsfine.domain.menu.service;

import com.eatsfine.eatsfine.domain.menu.dto.MenuReqDto;
import com.eatsfine.eatsfine.domain.menu.dto.MenuResDto;
import org.springframework.web.multipart.MultipartFile;

public interface MenuCommandService {
    MenuResDto.ImageUploadDto uploadImage(Long storeId, MultipartFile file);
    MenuResDto.ImageDeleteDto deleteImage(Long storeId, String imageKey);
    MenuResDto.MenuCreateDto createMenus(Long storeId, MenuReqDto.MenuCreateDto menuCreateDto);
    MenuResDto.MenuDeleteDto deleteMenus(Long storeId, MenuReqDto.MenuDeleteDto menuDeleteDto);

}
