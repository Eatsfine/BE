package com.eatsfine.eatsfine.domain.storetable.service;

import com.eatsfine.eatsfine.domain.storetable.dto.req.StoreTableReqDto;
import com.eatsfine.eatsfine.domain.storetable.dto.res.StoreTableResDto;

public interface StoreTableCommandService {
    StoreTableResDto.TableCreateDto createTable(Long storeId, StoreTableReqDto.TableCreateDto dto);

    StoreTableResDto.TableUpdateResultDto updateTable(Long storeId, Long tableId, StoreTableReqDto.TableUpdateDto dto);
}
