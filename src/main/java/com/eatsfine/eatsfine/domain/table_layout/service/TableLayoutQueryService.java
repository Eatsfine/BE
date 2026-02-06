package com.eatsfine.eatsfine.domain.table_layout.service;

import com.eatsfine.eatsfine.domain.table_layout.dto.res.TableLayoutResDto;

public interface TableLayoutQueryService {
    TableLayoutResDto.LayoutDetailDto getActiveLayout(Long storeId, String email);
}
