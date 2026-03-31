package com.eatsfine.domain.menu.service;

import com.eatsfine.domain.menu.dto.response.MenuResDto;

public interface MenuQueryService {
    MenuResDto.MenuListDto getMenus(Long storeId);
}
