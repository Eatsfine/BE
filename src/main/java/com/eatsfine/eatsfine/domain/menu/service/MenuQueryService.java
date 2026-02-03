package com.eatsfine.eatsfine.domain.menu.service;

import com.eatsfine.eatsfine.domain.menu.dto.MenuResDto;

public interface MenuQueryService {
    MenuResDto.MenuListDto getMenus(Long storeId);
}
