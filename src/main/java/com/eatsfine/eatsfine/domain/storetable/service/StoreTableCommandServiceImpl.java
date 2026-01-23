package com.eatsfine.eatsfine.domain.storetable.service;

import com.eatsfine.eatsfine.domain.store.exception.StoreException;
import com.eatsfine.eatsfine.domain.store.repository.StoreRepository;
import com.eatsfine.eatsfine.domain.store.status.StoreErrorStatus;
import com.eatsfine.eatsfine.domain.storetable.converter.StoreTableConverter;
import com.eatsfine.eatsfine.domain.storetable.dto.req.StoreTableReqDto;
import com.eatsfine.eatsfine.domain.storetable.dto.res.StoreTableResDto;
import com.eatsfine.eatsfine.domain.storetable.entity.StoreTable;
import com.eatsfine.eatsfine.domain.storetable.repository.StoreTableRepository;
import com.eatsfine.eatsfine.domain.storetable.validator.StoreTableValidator;
import com.eatsfine.eatsfine.domain.table_layout.entity.TableLayout;
import com.eatsfine.eatsfine.domain.table_layout.exception.TableLayoutException;
import com.eatsfine.eatsfine.domain.table_layout.exception.status.TableLayoutErrorStatus;
import com.eatsfine.eatsfine.domain.table_layout.repository.TableLayoutRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class StoreTableCommandServiceImpl implements StoreTableCommandService {
    private final StoreRepository storeRepository;
    private final TableLayoutRepository tableLayoutRepository;
    private final StoreTableRepository storeTableRepository;

    // 테이블 생성
    @Override
    public StoreTableResDto.TableCreateDto createTable(Long storeId, StoreTableReqDto.TableCreateDto dto) {
        storeRepository.findById(storeId)
                .orElseThrow(() -> new StoreException(StoreErrorStatus._STORE_NOT_FOUND));

        TableLayout layout = tableLayoutRepository.findByStoreIdAndIsActiveTrue(storeId)
                .orElseThrow(() -> new TableLayoutException(TableLayoutErrorStatus._LAYOUT_NOT_FOUND));

        // 좌석 범위 검증
        StoreTableValidator.validateSeatRange(dto.minSeatCount(), dto.maxSeatCount());

        // 테이블이 그리드 범위 내인지 검증 (테이블 생성 시 크기는 1x1 크기로 고정)
        StoreTableValidator.validateGridBounds(dto.gridX(), dto.gridY(), 1, 1, layout);

        // 테이블 겹침 검증
        StoreTableValidator.validateNoOverlap(dto.gridX(), dto.gridY(), 1, 1, layout.getTables());

        // 테이블 번호 자동 생성
        String tableNumber = generateTableNumber(layout);

        // 테이블 생성
        StoreTable newTable = StoreTable.builder()
                .tableNumber(tableNumber)
                .tableLayout(layout)
                .gridX(dto.gridX())
                .gridY(dto.gridY())
                .widthSpan(1)
                .heightSpan(1)
                .minSeatCount(dto.minSeatCount())
                .maxSeatCount(dto.maxSeatCount())
                .seatsType(dto.seatsType())
                .rating(BigDecimal.ZERO)
                .tableImageUrl(dto.tableImageUrl())
                .isDeleted(false)
                .build();

        StoreTable savedTable = storeTableRepository.save(newTable);

        return StoreTableConverter.toTableCreateDto(savedTable);
    }

    private String generateTableNumber(TableLayout layout) {
        List<StoreTable> tables = layout.getTables();

        if (tables.isEmpty()) {
            return "1번 테이블";
        }

        // 기존 테이블 번호 중 최대값 찾기
        int maxNumber = tables.stream()
                .map(StoreTable::getTableNumber)
                .filter(number -> number.matches("\\d+번 테이블"))
                .map(number -> {
                    String numPart = number.replace("번 테이블", "");
                    return Integer.parseInt(numPart);
                })
                .max(Integer::compareTo)
                .orElse(0);

        return String.format("%d번 테이블", maxNumber + 1);
    }
}
