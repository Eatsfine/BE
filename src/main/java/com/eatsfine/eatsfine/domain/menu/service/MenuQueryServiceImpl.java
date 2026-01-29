package com.eatsfine.eatsfine.domain.menu.service;

import com.eatsfine.eatsfine.domain.menu.dto.MenuResDto;
import com.eatsfine.eatsfine.domain.store.dto.StoreResDto;
import com.eatsfine.eatsfine.domain.store.entity.Store;
import com.eatsfine.eatsfine.domain.store.exception.StoreException;
import com.eatsfine.eatsfine.domain.store.repository.StoreRepository;
import com.eatsfine.eatsfine.domain.store.status.StoreErrorStatus;
import com.eatsfine.eatsfine.global.s3.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MenuQueryServiceImpl implements  MenuQueryService {
    private final StoreRepository storeRepository;
    private final S3Service s3Service;

    @Override
    public MenuResDto.MenuListDto getMenus(Long storeId) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new StoreException(StoreErrorStatus._STORE_NOT_FOUND));

        List<MenuResDto.MenuDetailDto> menuDtos = store.getMenus().stream()
                .map(menu -> MenuResDto.MenuDetailDto.builder()
                        .menuId(menu.getId())
                        .name(menu.getName())
                        .description(menu.getDescription())
                        .price(menu.getPrice())
                        .category(menu.getMenuCategory())
                        .imageUrl(s3Service.toUrl(menu.getImageKey()))
                        .isSoldOut(menu.isSoldOut())
                        .build()
                )
                .toList();

        return MenuResDto.MenuListDto.builder()
                .menus(menuDtos)
                .build();
    }
}
