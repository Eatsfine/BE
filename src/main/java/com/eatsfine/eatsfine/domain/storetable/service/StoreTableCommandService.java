package com.eatsfine.eatsfine.domain.storetable.service;

import com.eatsfine.eatsfine.domain.storetable.dto.req.StoreTableReqDto;
import com.eatsfine.eatsfine.domain.storetable.dto.res.StoreTableResDto;
import org.springframework.web.multipart.MultipartFile;

public interface StoreTableCommandService {
    StoreTableResDto.TableCreateDto createTable(Long storeId, StoreTableReqDto.TableCreateDto dto);

    StoreTableResDto.TableUpdateResultDto updateTable(Long storeId, Long tableId, StoreTableReqDto.TableUpdateDto dto);

    StoreTableResDto.TableDeleteDto deleteTable(Long storeId, Long tableId);

    StoreTableResDto.UploadTableImageDto uploadTableImage(Long storeId, Long tableId, MultipartFile tableImage);

    StoreTableResDto.DeleteTableImageDto deleteTableImage(Long storeId, Long tableId);
}
