package com.eatsfine.eatsfine.domain.tableimage.converter;

import com.eatsfine.eatsfine.domain.tableimage.dto.TableImageResDto;
import com.eatsfine.eatsfine.domain.tableimage.entity.TableImage;

import java.util.List;

public class TableImageConverter {

    public static TableImageResDto.UploadTableImageDto toTableImageDto(Long storeId, List<String> tableImages) {
        return TableImageResDto.UploadTableImageDto.builder()
                .storeId(storeId)
                .tableImages(tableImages)
                .build();
    }
}
