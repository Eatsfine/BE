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
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Transaction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
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
                            .build();

                    // 임시 이미지 키가 있는 경우, 영구 경로로 이동하고 키를 설정
                    String tempImageKey = menuDto.imageKey();
                    if (tempImageKey != null && !tempImageKey.isBlank()) {
                        // 1. 새로운 영구 키 생성
                        String extension = s3Service.extractExtension(tempImageKey);
                        String permanentImageKey = "stores/" + storeId + "/menus/" + UUID.randomUUID() + extension;

                        // 2. S3에서 객체 이동 (임시 -> 영구)
                        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                        @Override
                        public void afterCommit(){
                            try{
                                s3Service.moveObject(tempImageKey, permanentImageKey);
                            } catch (Exception e) {
                                log.error("temp에서 영구로 이동 실패. Source: {}, Dest: {}", tempImageKey, permanentImageKey);
                            }
                        }

                        });

                        // 3. 엔티티에 영구 키 저장
                        menu.updateImageKey(permanentImageKey);
                    }

                    store.addMenu(menu);
                    return menu;
                })
                .toList();

        List<Menu> savedMenus = menuRepository.saveAll(menus);

        List<MenuResDto.MenuDto> menuDtos = savedMenus.stream().map(
                menu -> MenuResDto.MenuDto.builder()
                        .menuId(menu.getId())
                        .name(menu.getName())
                        .description(menu.getDescription())
                        .price(menu.getPrice())
                        .category(menu.getMenuCategory())
                        .imageUrl(s3Service.toUrl(menu.getImageKey()))
                        .build())
                .toList();

        return MenuConverter.toCreateDto(menuDtos);
    }

    @Override
    public MenuResDto.MenuDeleteDto deleteMenus(Long storeId, MenuReqDto.MenuDeleteDto dto) {
        Store store = findAndVerifyStore(storeId);

        List<Long> menuIds = dto.menuIds();
        List<Menu> menusToDelete = menuRepository.findAllById(dto.menuIds());

        if(menusToDelete.size() != menuIds.size()) {
            throw new MenuException(MenuErrorStatus._MENU_NOT_FOUND);
        }

        // 1. 모든 메뉴가 해당 가게 소유인지 확인하고, S3 이미지 삭제
        menusToDelete.forEach(menu -> {
            verifyMenuBelongsToStore(menu, storeId);
            // Soft Delete 시 연결된 S3 이미지도 함께 삭제
            if (menu.getImageKey() != null && !menu.getImageKey().isBlank()) {
                String imageKey = menu.getImageKey();
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        s3Service.deleteByKey(imageKey);
                    }
                });
            }
        });

        // 2. DB에서 Soft Delete 실행
        // Menu 엔티티의 @SQLDelete 어노테이션 덕분에 deleteAll이 UPDATE로 동작함
        menuRepository.deleteAll(menusToDelete);

        // 3. Store 컬렉션에서 제거
        store.getMenus().removeAll(menusToDelete);

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
            // 1. [Safety] 변경된 내용이 없으면 스킵 (프론트에서 기존 키를 그대로 보낸 경우)
            if (newImageKey.equals(menu.getImageKey())) {
                return;
            }

            // 새로운 이미지가 있다면 영구 경로로 이동 (Temp -> Perm)
            if (newImageKey != null && !newImageKey.isBlank()) {
                String extension = s3Service.extractExtension(newImageKey);
                String permanentImageKey = "stores/" + storeId + "/menus/" + UUID.randomUUID() + extension;
                String oldImageKey = menu.getImageKey();

                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        try {
                            s3Service.moveObject(newImageKey, permanentImageKey);
                            if (oldImageKey != null && !oldImageKey.isBlank()) {
                                s3Service.deleteByKey(oldImageKey);
                            }
                        }
                        catch (Exception e) {
                            log.error("메뉴 이미지를 s3에 업데이트하는 데에 실패했습니다.", e);
                        }
                    }
                });

                menu.updateImageKey(permanentImageKey);

            } else {
                // 빈 문자열("")인 경우 -> 이미지 삭제 요청
                if (menu.getImageKey() != null && !menu.getImageKey().isBlank()) {
                    String oldImageKey = menu.getImageKey();
                    TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                        @Override
                        public void afterCommit() {
                            s3Service.deleteByKey(oldImageKey);
                        }
                    });
                }
                menu.updateImageKey(null);
            }
        });

        String updatedImageUrl = s3Service.toUrl(menu.getImageKey());

        return MenuConverter.toUpdateDto(menu, updatedImageUrl);
    }

    @Override
    public MenuResDto.SoldOutUpdateDto updateSoldOutStatus(Long storeId, Long menuId, boolean isSoldOut) {
        findAndVerifyStore(storeId);

        Menu menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new MenuException(MenuErrorStatus._MENU_NOT_FOUND));

        verifyMenuBelongsToStore(menu, storeId);

        // 기존 값과 동일하다면 바로 리턴
        if(menu.isSoldOut() == isSoldOut) {
            return MenuConverter.toSoldOutUpdateDto(menu);
        }

        menu.updateSoldOut(isSoldOut);

        return MenuConverter.toSoldOutUpdateDto(menu);

    }

    @Override
    public MenuResDto.ImageUploadDto uploadImage(Long storeId, MultipartFile file) {
        Store store = findAndVerifyStore(storeId);

        if(file.isEmpty()) {
            throw new ImageException(ImageErrorStatus.EMPTY_FILE);
        }

        // 이미지를 항상 임시 경로에 업로드
        String tempPath = "temp/menus";
        String imageKey = s3Service.upload(file, tempPath);

        return MenuConverter.toImageUploadDto(imageKey, s3Service.toUrl(imageKey));
    }

    @Override
    public MenuResDto.ImageDeleteDto deleteMenuImage(Long storeId, Long menuId) {
        findAndVerifyStore(storeId);

        Menu menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new MenuException(MenuErrorStatus._MENU_NOT_FOUND));

        verifyMenuBelongsToStore(menu, storeId);

        String imageKey = menu.getImageKey();

        if (imageKey == null || imageKey.isBlank()) {
            // 이미지가 없는 메뉴에 삭제 요청이 온 경우, 예외
            throw new ImageException(ImageErrorStatus._IMAGE_NOT_FOUND);
        }

        // 1. S3에서 파일 삭제
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                s3Service.deleteByKey(imageKey);
            }
        });

        // 2. DB에서 imageKey를 null로 업데이트 (Dirty Checking)
        menu.updateImageKey(null);

        return MenuConverter.toImageDeleteDto(imageKey); // 삭제된 이미지의 키를 반환
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
            throw new StoreException(StoreErrorStatus._NOT_STORE_OWNER);
        }
    }
}
