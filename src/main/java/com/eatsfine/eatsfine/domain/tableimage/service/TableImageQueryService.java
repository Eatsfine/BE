package com.eatsfine.eatsfine.domain.tableimage.service;

import com.eatsfine.eatsfine.domain.tableimage.dto.TableImageResDto;

public interface TableImageQueryService {
    TableImageResDto.GetTableImageDto getTableImage(Long storeId);
}
