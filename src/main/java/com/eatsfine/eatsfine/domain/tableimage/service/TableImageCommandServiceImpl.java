package com.eatsfine.eatsfine.domain.tableimage.service;

import com.eatsfine.eatsfine.domain.image.exception.ImageException;
import com.eatsfine.eatsfine.domain.image.status.ImageErrorStatus;
import com.eatsfine.eatsfine.domain.store.entity.Store;
import com.eatsfine.eatsfine.domain.store.exception.StoreException;
import com.eatsfine.eatsfine.domain.store.repository.StoreRepository;
import com.eatsfine.eatsfine.domain.store.status.StoreErrorStatus;
import com.eatsfine.eatsfine.domain.store.validator.StoreValidator;
import com.eatsfine.eatsfine.domain.tableimage.converter.TableImageConverter;
import com.eatsfine.eatsfine.domain.tableimage.dto.TableImageResDto;
import com.eatsfine.eatsfine.domain.tableimage.entity.TableImage;
import com.eatsfine.eatsfine.domain.tableimage.repository.TableImageRepository;
import com.eatsfine.eatsfine.global.s3.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class TableImageCommandServiceImpl implements TableImageCommandService {

    private final StoreRepository storeRepository;
    private final TableImageRepository tableImageRepository;
    private final S3Service s3Service;
    private final StoreValidator storeValidator;

    // 가게 테이블 이미지 등록
    public TableImageResDto.UploadTableImageDto uploadTableImage(Long storeId, List<MultipartFile> files, String email) {

        Store store = storeValidator.validateStoreOwner(storeId, email);

        if(files == null || files.isEmpty() || files.stream().allMatch(MultipartFile::isEmpty)) {
            throw new ImageException(ImageErrorStatus.EMPTY_FILE);
        }

        int imageOrder = tableImageRepository.findMaxOrderByStoreId(storeId) + 1;
        List<String> tableImages = new ArrayList<>();

        for (MultipartFile file : files) {
            String key = s3Service.upload(file, "stores/" + storeId + "/tables");
            TableImage tableImage = TableImage.builder()
                    .tableImageKey(key)
                    .imageOrder(imageOrder++)
                    .build();
            store.addTableImage(tableImage);
            tableImages.add(s3Service.toUrl(key));
        }
        return TableImageConverter.toUploadTableImageDto(storeId, tableImages);
    }

    @Override
    public TableImageResDto.DeleteTableImageDto deleteTableImage(Long storeId, List<Long> tableImageIds, String email) {

        Store store = storeValidator.validateStoreOwner(storeId, email);

        List<TableImage> tableImages = tableImageIds.stream()
                        .map(id -> tableImageRepository.findByIdAndStore(id, store)
                                .orElseThrow(() -> new ImageException(ImageErrorStatus._IMAGE_NOT_FOUND)))
                .toList();

        for (TableImage tableImage : tableImages) {
            s3Service.deleteByKey(tableImage.getTableImageKey());
            store.removeTableImage(tableImage);
        }

        return TableImageConverter.toDeleteTableImageDto(storeId, tableImageIds);
    }
}
