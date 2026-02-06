package com.eatsfine.eatsfine.domain.storetable.service;

import com.eatsfine.eatsfine.domain.storetable.dto.req.StoreTableReqDto;
import com.eatsfine.eatsfine.domain.storetable.dto.res.StoreTableResDto;
import org.springframework.web.multipart.MultipartFile;

public interface StoreTableCommandService {
    StoreTableResDto.TableCreateDto createTable(Long storeId, StoreTableReqDto.TableCreateDto dto, String email);

    StoreTableResDto.ImageUploadDto uploadTableImageTemp(Long storeId, MultipartFile file, String email);

    StoreTableResDto.TableUpdateResultDto updateTable(Long storeId, Long tableId, StoreTableReqDto.TableUpdateDto dto, String email);

    StoreTableResDto.TableDeleteDto deleteTable(Long storeId, Long tableId, String email);

    StoreTableResDto.UploadTableImageDto uploadTableImage(Long storeId, Long tableId, MultipartFile tableImage, String email);

    StoreTableResDto.DeleteTableImageDto deleteTableImage(Long storeId, Long tableId, String email);
}
