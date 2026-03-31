package com.eatsfine.domain.tableimage.converter;

import com.eatsfine.domain.tableimage.dto.response.TableImageResDto;

import java.util.List;

public class TableImageConverter {

    public static TableImageResDto.UploadTableImageDto toUploadTableImageDto(Long storeId, List<String> tableImages) {
        return TableImageResDto.UploadTableImageDto.builder()
                .storeId(storeId)
                .tableImageUrls(tableImages)
                .build();
    }

    public static TableImageResDto.GetTableImageDto toGetTableImageDto(Long storeId,
            List<TableImageResDto.TableImageItem> tableImages) {
        return TableImageResDto.GetTableImageDto.builder()
                .storeId(storeId)
                .tableImages(tableImages)
                .build();
    }

    public static TableImageResDto.DeleteTableImageDto toDeleteTableImageDto(Long storeId,
            List<Long> removedTableImages) {
        return TableImageResDto.DeleteTableImageDto.builder()
                .storeId(storeId)
                .deletedTableImageIds(removedTableImages)
                .build();
    }
}
