package com.eatsfine.eatsfine.domain.menu.service;

import com.eatsfine.eatsfine.domain.image.exception.ImageException;
import com.eatsfine.eatsfine.domain.image.status.ImageErrorStatus;
import com.eatsfine.eatsfine.domain.menu.converter.MenuConverter;
import com.eatsfine.eatsfine.domain.menu.dto.MenuReqDto;
import com.eatsfine.eatsfine.domain.menu.dto.MenuResDto;
import com.eatsfine.eatsfine.domain.menu.entity.Menu;
import com.eatsfine.eatsfine.domain.menu.exception.MenuException;
import com.eatsfine.eatsfine.domain.menu.repository.MenuRepository;
import com.eatsfine.eatsfine.domain.menu.status.MenuErrorStatus;
import com.eatsfine.eatsfine.domain.store.entity.Store;
import com.eatsfine.eatsfine.domain.store.exception.StoreException;
import com.eatsfine.eatsfine.domain.store.repository.StoreRepository;
import com.eatsfine.eatsfine.domain.store.status.StoreErrorStatus;
import com.eatsfine.eatsfine.global.s3.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class MenuCommandServiceImpl implements MenuCommandService {

    private final S3Service s3Service;
    private final StoreRepository storeRepository;
    private final MenuRepository menuRepository;

    @Override
    public MenuResDto.MenuCreateDto createMenus(Long storeId, MenuReqDto.MenuCreateDto dto) {
        Store store = findAndVerifyStore(storeId);

        List<Menu> menus = dto.menus().stream()
                .map(menuDto -> {
                    Menu menu = Menu.builder()
                            .name(menuDto.name())
                            .description(menuDto.description())
                            .price(menuDto.price())
                            .menuCategory(menuDto.category())
                            .imageKey(menuDto.imageKey())
                            .build();
                    store.addMenu(menu);
                    return menu;
                })
                .toList();
        menuRepository.saveAll(menus);

        return MenuConverter.toCreateDto(menus);
    }

    @Override
    public MenuResDto.MenuDeleteDto deleteMenus(Long storeId, MenuReqDto.MenuDeleteDto dto) {
        Store store = findAndVerifyStore(storeId);

        List<Long> menuIds = dto.menuIds();
        List<Menu> menusToDelete = menuRepository.findAllById(dto.menuIds());

        if(menusToDelete.size() != menuIds.size()) {
            throw new MenuException(MenuErrorStatus._MENU_NOT_FOUND);
        }

        // 모든 메뉴가 해당 가게 소유인지 확인하고, 부모 컬렉션에서 제거
        menusToDelete.forEach(menu -> {
            verifyMenuBelongsToStore(menu, storeId);
            store.removeMenu(menu);
        });

        return MenuConverter.toDeleteDto(menuIds);
    }

    @Override
    public MenuResDto.MenuUpdateDto updateMenu(Long storeId, Long menuId, MenuReqDto.MenuUpdateDto dto) {
        Store store = findAndVerifyStore(storeId);

        // TODO: [보안] Spring Security 병합 후, 현재 로그인한 사용자가 이 가게의 주인인지 확인하는 로직 추가

        Menu menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new MenuException(MenuErrorStatus._MENU_NOT_FOUND));

        verifyMenuBelongsToStore(menu, storeId);

        // 이름, 설명, 가격, 카테고리 업데이트
        Optional.ofNullable(dto.name()).ifPresent(menu::updateName);
        Optional.ofNullable(dto.description()).ifPresent(menu::updateDescription);
        Optional.ofNullable(dto.price()).ifPresent(menu::updatePrice);
        Optional.ofNullable(dto.category()).ifPresent(menu::updateCategory);

        Optional.ofNullable(dto.imageKey()).ifPresent(newImageKey -> {
            // 기존 이미지가 있다면 S3에서 삭제
            if(menu.getImageKey() != null && !menu.getImageKey().isBlank()) {
                s3Service.deleteByKey(menu.getImageKey());
            }
            // 새로운 이미지 키로 업데이트
            menu.updateImageKey(newImageKey);
        });

        return MenuConverter.toUpdateDto(menu);
    }

    @Override
    public MenuResDto.SoldOutUpdateDto updateSoldOutStatus(Long storeId, Long menuId, boolean isSoldOut) {
        findAndVerifyStore(storeId);

        Menu menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new MenuException(MenuErrorStatus._MENU_NOT_FOUND));

        verifyMenuBelongsToStore(menu, storeId);

        menu.updateSoldOut(isSoldOut);

        return MenuConverter.toSoldOutUpdateDto(menu);

    }

    @Override
    public MenuResDto.ImageUploadDto uploadImage(Long storeId, MultipartFile file) {
        Store store = findAndVerifyStore(storeId);

        if(file.isEmpty()) {
            throw new ImageException(ImageErrorStatus.EMPTY_FILE);
        }

        String path = "stores/" + storeId + "/menus";
        String key = s3Service.upload(file, path);

        return MenuConverter.toImageUploadDto(key);
    }

    @Override
    public MenuResDto.ImageDeleteDto deleteImage(Long storeId, String imageKey) {
        // TODO: [보안] Spring Security 병합 후, 현재 로그인한 사용자가 이 메뉴가 속한 가게의 주인인지 확인

        // imageKey로 DB에서 메뉴 찾아봄
        Optional<Menu> menuOptional = menuRepository.findByImageKey(imageKey);

        // 이미 등록 완료된 사진인 경우에는 db에서 null처리
        menuOptional.ifPresent(menu -> {
            verifyMenuBelongsToStore(menu, storeId);
            menu.updateImageKey(null);
        });

        s3Service.deleteByKey(imageKey);
        return MenuConverter.toImageDeleteDto(imageKey);
    }

    private Store findAndVerifyStore(Long storeId) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new StoreException(StoreErrorStatus._STORE_NOT_FOUND));
        // TODO: [보안] Spring Security 병합 후, 현재 로그인한 사용자가 이 가게의 주인인지 확인하는 로직 추가
        return store;
    }

    private void verifyMenuBelongsToStore(Menu menu, Long storeId) {
        if (!menu.getStore().getId().equals(storeId)) {
            // 다른 가게의 메뉴를 조작하려는 시도 방지
            throw new StoreException(StoreErrorStatus._STORE_NOT_OWNER);
        }
    }
}
