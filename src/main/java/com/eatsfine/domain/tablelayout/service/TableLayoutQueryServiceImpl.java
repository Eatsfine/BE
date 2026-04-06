package com.eatsfine.domain.tablelayout.service;

import com.eatsfine.domain.store.exception.StoreException;
import com.eatsfine.domain.store.repository.StoreRepository;
import com.eatsfine.domain.store.status.StoreErrorStatus;
import com.eatsfine.domain.store.validator.StoreValidator;
import com.eatsfine.domain.tablelayout.converter.TableLayoutConverter;
import com.eatsfine.domain.tablelayout.dto.response.TableLayoutResDto;
import com.eatsfine.domain.tablelayout.repository.TableLayoutRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TableLayoutQueryServiceImpl implements TableLayoutQueryService {
    private final StoreRepository storeRepository;
    private final TableLayoutRepository tableLayoutRepository;
    private final StoreValidator storeValidator;

    // 테이블 배치도 조회
    @Override
    public TableLayoutResDto.LayoutDetailDto getActiveLayout(Long storeId, String email) {

        storeValidator.validateStoreOwner(storeId, email);

        // 배치도가 없을 시 null 반환
        return tableLayoutRepository.findByStoreIdAndIsActiveTrue(storeId)
                .map(TableLayoutConverter::toLayoutDetailDto)
                .orElse(null);
    }
}
