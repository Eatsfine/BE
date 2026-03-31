package com.eatsfine.domain.tablelayout.service;

import com.eatsfine.domain.tablelayout.dto.request.TableLayoutReqDto;
import com.eatsfine.domain.tablelayout.dto.response.TableLayoutResDto;

public interface TableLayoutCommandService {
    TableLayoutResDto.LayoutDetailDto createLayout(Long storeId, TableLayoutReqDto.LayoutCreateDto dto, String email);
}
