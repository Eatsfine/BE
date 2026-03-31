package com.eatsfine.domain.tableimage.service;

import com.eatsfine.domain.tableimage.dto.response.TableImageResDto;

public interface TableImageQueryService {
    TableImageResDto.GetTableImageDto getTableImage(Long storeId);
}
