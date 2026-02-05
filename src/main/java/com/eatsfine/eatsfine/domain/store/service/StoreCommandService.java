package com.eatsfine.eatsfine.domain.store.service;

import com.eatsfine.eatsfine.domain.store.dto.StoreReqDto;
import com.eatsfine.eatsfine.domain.store.dto.StoreResDto;
import org.springframework.web.multipart.MultipartFile;

public interface StoreCommandService {
    StoreResDto.StoreCreateDto createStore(StoreReqDto.StoreCreateDto storeCreateDto, String email);
    StoreResDto.StoreUpdateDto updateBasicInfo(Long storeId, StoreReqDto.StoreUpdateDto storeUpdateDto, String email);
    StoreResDto.UploadMainImageDto uploadMainImage(Long storeId, MultipartFile file, String email);
}
