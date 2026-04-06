package com.eatsfine.domain.tablelayout.service;

import com.eatsfine.domain.tablelayout.dto.response.TableLayoutResDto;

public interface TableLayoutQueryService {
    TableLayoutResDto.LayoutDetailDto getActiveLayout(Long storeId, String email);
}
