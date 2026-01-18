package com.eatsfine.eatsfine.domain.table_layout.service;

import com.eatsfine.eatsfine.domain.table_layout.dto.req.TableLayoutReqDto;
import com.eatsfine.eatsfine.domain.table_layout.dto.res.TableLayoutResDto;

public interface TableLayoutCommandService {
    TableLayoutResDto.LayoutDetailDto createLayout(Long storeId, TableLayoutReqDto.LayoutCreateDto dto);
}
