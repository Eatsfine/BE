package com.eatsfine.eatsfine.domain.storetable.service;

import com.eatsfine.eatsfine.domain.store.exception.StoreException;
import com.eatsfine.eatsfine.domain.store.repository.StoreRepository;
import com.eatsfine.eatsfine.domain.store.status.StoreErrorStatus;
import com.eatsfine.eatsfine.domain.storetable.converter.StoreTableConverter;
import com.eatsfine.eatsfine.domain.storetable.dto.req.StoreTableReqDto;
import com.eatsfine.eatsfine.domain.storetable.dto.res.StoreTableResDto;
import com.eatsfine.eatsfine.domain.storetable.entity.StoreTable;
import com.eatsfine.eatsfine.domain.storetable.exception.StoreTableException;
import com.eatsfine.eatsfine.domain.storetable.exception.status.StoreTableErrorStatus;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

    // 테이블 정보 수정
    @Override
    public StoreTableResDto.TableUpdateResultDto updateTable(Long storeId, Long tableId, StoreTableReqDto.TableUpdateDto dto) {
        // 최소 하나의 변경사항이 있는지 확인
        if (!dto.hasAnyUpdate()) {
            throw new StoreTableException(StoreTableErrorStatus._NO_UPDATE_FIELD);
        }

        storeRepository.findById(storeId)
                .orElseThrow(() -> new StoreException(StoreErrorStatus._STORE_NOT_FOUND));

        StoreTable table = storeTableRepository.findById(tableId)
                .orElseThrow(() -> new StoreTableException(StoreTableErrorStatus._TABLE_NOT_FOUND));

        StoreTableValidator.validateTableBelongsToStore(table, storeId);

        // 변경된 테이블 리스트
        List<StoreTable> affectedTables = new ArrayList<>();
        affectedTables.add(table);

        // 테이블 번호 수정
        if (dto.tableNumber() != null) {
            List<StoreTable> swappedTables = updateTableNumber(table, dto.tableNumber());
            // 스왑된 테이블이 있으면 추가
            swappedTables.stream()
                    .filter(t -> !t.getId().equals(table.getId()))
                    .forEach(affectedTables::add);
        }

        // 테이블 좌석 수 변경
        if (dto.minSeatCount() != null || dto.maxSeatCount() != null) {
            // null인 필드는 기존 값 유지
            int finalMin = dto.minSeatCount() != null ? dto.minSeatCount() : table.getMinSeatCount();
            int finalMax = dto.maxSeatCount() != null ? dto.maxSeatCount() : table.getMaxSeatCount();

            // 기존 값과 동일한지 검증
            if (finalMin == table.getMinSeatCount() && finalMax == table.getMaxSeatCount()) {
                throw new StoreTableException(StoreTableErrorStatus._SAME_SEAT_COUNT);
            }

            StoreTableValidator.validateSeatRange(finalMin, finalMax);

            table.updateSeatCount(finalMin, finalMax);
        }

        // 테이블 유형 변경
        if (dto.seatsType() != null) {
            if (table.getSeatsType() == dto.seatsType()) {
                throw new StoreTableException(StoreTableErrorStatus._SAME_SEATS_TYPE);
            }
            table.updateSeatsType(dto.seatsType());
        }

        return StoreTableConverter.toTableUpdateResultDto(affectedTables, dto);
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

    private List<StoreTable> updateTableNumber(StoreTable table, String newNumber) {
        String newTableNumber = String.format("%s번 테이블", newNumber);
        String currentTableNumber = table.getTableNumber();

        List<StoreTable> updatedTables = new ArrayList<>();
        updatedTables.add(table);

        // 기존 번호와 동일하면 변경 불필요
        if (currentTableNumber.equals(newTableNumber)) {
            return updatedTables;
        }

        // 같은 레이아웃에서 새 번호를 가진 테이블이 있는지 확인
        Optional<StoreTable> existingTable = storeTableRepository
                .findByTableLayoutAndTableNumberAndIsDeletedFalse(
                        table.getTableLayout(),
                        newTableNumber
                );

        // 중복된 번호를 가진 테이블이 있으면 스왑
        if (existingTable.isPresent()) {
            StoreTable conflictTable = existingTable.get();
            conflictTable.updateTableNumber(currentTableNumber);
            updatedTables.add(conflictTable);
        }

        // 대상 테이블의 번호 변경
        table.updateTableNumber(newTableNumber);

        return updatedTables;
    }
}
