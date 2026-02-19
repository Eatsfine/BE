package com.eatsfine.eatsfine.domain.tableimage.service;

import com.eatsfine.eatsfine.domain.store.entity.Store;
import com.eatsfine.eatsfine.domain.store.exception.StoreException;
import com.eatsfine.eatsfine.domain.store.repository.StoreRepository;
import com.eatsfine.eatsfine.domain.store.status.StoreErrorStatus;
import com.eatsfine.eatsfine.domain.tableimage.converter.TableImageConverter;
import com.eatsfine.eatsfine.domain.tableimage.dto.TableImageResDto;
import com.eatsfine.eatsfine.domain.tableimage.entity.TableImage;
import com.eatsfine.eatsfine.domain.tableimage.repository.TableImageRepository;
import com.eatsfine.eatsfine.global.s3.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TableImageQueryServiceImpl implements TableImageQueryService {

    private final StoreRepository storeRepository;
    private final TableImageRepository tableImageRepository;
    private final S3Service s3Service;

    @Override
    public TableImageResDto.GetTableImageDto getTableImage(Long storeId) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new StoreException(StoreErrorStatus._STORE_NOT_FOUND));

        List<TableImage> tableImageEntities = tableImageRepository.findAllByStoreOrderByImageOrder(store);

        List<TableImageResDto.TableImageItem> tableItems = tableImageEntities.stream()
                .map(ti -> TableImageResDto.TableImageItem.builder()
                        .tableImageId(ti.getId())
                        .tableImageUrl(s3Service.toUrl(ti.getTableImageKey()))
                        .build())
                .toList();

        return TableImageConverter.toGetTableImageDto(storeId, tableItems);
    }

}
