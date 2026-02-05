package com.eatsfine.eatsfine.domain.table_layout.service;

import com.eatsfine.eatsfine.domain.store.entity.Store;
import com.eatsfine.eatsfine.domain.store.exception.StoreException;
import com.eatsfine.eatsfine.domain.store.repository.StoreRepository;
import com.eatsfine.eatsfine.domain.store.status.StoreErrorStatus;
import com.eatsfine.eatsfine.domain.store.validator.StoreValidator;
import com.eatsfine.eatsfine.domain.table_layout.converter.TableLayoutConverter;
import com.eatsfine.eatsfine.domain.table_layout.dto.req.TableLayoutReqDto;
import com.eatsfine.eatsfine.domain.table_layout.dto.res.TableLayoutResDto;
import com.eatsfine.eatsfine.domain.table_layout.entity.TableLayout;
import com.eatsfine.eatsfine.domain.table_layout.repository.TableLayoutRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class TableLayoutCommandServiceImpl implements TableLayoutCommandService {
    private final StoreRepository storeRepository;
    private final TableLayoutRepository tableLayoutRepository;
    private final StoreValidator storeValidator;

    // 테이블 배치도 생성
    @Override
    public TableLayoutResDto.LayoutDetailDto createLayout(
            Long storeId,
            TableLayoutReqDto.LayoutCreateDto dto,
            String email
            ) {

        Store store = storeValidator.validateStoreOwner(storeId, email);

        deactivateExistingLayout(store);

        // 새 배치도 생성
        TableLayout newLayout = TableLayout.builder()
                .store(store)
                .lows(dto.gridRow())
                .cols(dto.gridCol())
                .isActive(true)
                .isDeleted(false)
                .build();

        TableLayout savedLayout = tableLayoutRepository.save(newLayout);

        return TableLayoutConverter.toLayoutDetailDto(savedLayout);
    }

    // 기존 테이블 배치도 비활성화
    private void deactivateExistingLayout(Store store) {
        tableLayoutRepository.findByStoreIdAndIsActiveTrue(store.getId())
                .ifPresent(tableLayoutRepository::delete);
    }
}
