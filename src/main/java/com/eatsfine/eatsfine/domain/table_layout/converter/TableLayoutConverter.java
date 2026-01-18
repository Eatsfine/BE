package com.eatsfine.eatsfine.domain.table_layout.converter;

import com.eatsfine.eatsfine.domain.storetable.entity.StoreTable;
import com.eatsfine.eatsfine.domain.table_layout.dto.res.TableLayoutResDto;
import com.eatsfine.eatsfine.domain.table_layout.entity.TableLayout;

public class TableLayoutConverter {
    // TableLayout Entity를 생성 응답 DTO로 변환
    public static TableLayoutResDto.LayoutDetailDto toLayoutDetailDto(TableLayout layout) {
        return TableLayoutResDto.LayoutDetailDto.builder()
                .layoutId(layout.getId())
                .totalTableCount(layout.getTables().size())
                .gridInfo(
                        TableLayoutResDto.GridInfo.builder()
                                .gridCol(layout.getCols())
                                .gridRow(layout.getLows())
                                .build()
                )
                .tables(
                        layout.getTables().stream()
                                .map(TableLayoutConverter::toTableInfo)
                                .toList()
                )
                .build();
    }

    // StoreTable Entity를 TableInfo DTO로 변환
    private static TableLayoutResDto.TableInfo toTableInfo(StoreTable table) {
        return TableLayoutResDto.TableInfo.builder()
                .tableId(table.getId())
                .tableNumber(table.getTableNumber())
                .seatsType(table.getSeatsType())
                .minSeatCount(table.getMinSeatCount())
                .maxSeatCount(table.getMaxSeatCount())
                .gridX(table.getGridX())
                .gridY(table.getGridY())
                .widthSpan(table.getWidthSpan())
                .heightSpan(table.getHeightSpan())
                .build();
    }
}
