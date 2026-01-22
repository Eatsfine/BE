package com.eatsfine.eatsfine.domain.tableimage.dto;

import lombok.Builder;

import java.util.List;

public class TableImageResDto {

    @Builder
    public record UploadTableImageDto(
            Long storeId,
            List<String> tableImageUrls
    ){}

    @Builder
    public record GetTableImageDto(
            Long storeId,
            List<String> tableImageUrls
    ){}

    @Builder
    public record DeleteTableImageDto(
            Long storeId,
            List<Long> deletedTableImageIds
    ){}
}
