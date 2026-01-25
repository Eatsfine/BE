package com.eatsfine.eatsfine.domain.storetable.service;

import com.eatsfine.eatsfine.domain.booking.repository.BookingRepository;
import com.eatsfine.eatsfine.domain.store.exception.StoreException;
import com.eatsfine.eatsfine.domain.store.repository.StoreRepository;
import com.eatsfine.eatsfine.domain.store.status.StoreErrorStatus;
import com.eatsfine.eatsfine.domain.storetable.converter.StoreTableConverter;
import com.eatsfine.eatsfine.domain.storetable.dto.res.StoreTableResDto;
import com.eatsfine.eatsfine.domain.storetable.entity.StoreTable;
import com.eatsfine.eatsfine.domain.storetable.exception.StoreTableException;
import com.eatsfine.eatsfine.domain.storetable.exception.status.StoreTableErrorStatus;
import com.eatsfine.eatsfine.domain.storetable.repository.StoreTableRepository;
import com.eatsfine.eatsfine.domain.storetable.util.SlotCalculator;
import com.eatsfine.eatsfine.domain.storetable.validator.StoreTableValidator;
import com.eatsfine.eatsfine.domain.tableblock.entity.TableBlock;
import com.eatsfine.eatsfine.domain.tableblock.repository.TableBlockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StoreTableQueryServiceImpl implements StoreTableQueryService{
    private final StoreRepository storeRepository;
    private final StoreTableRepository storeTableRepository;
    private final TableBlockRepository tableBlockRepository;
    private final BookingRepository bookingRepository;

    // 테이블 슬롯 조회
    @Override
    public StoreTableResDto.SlotListDto getTableSlots(Long storeId, Long tableId, LocalDate date) {
        storeRepository.findById(storeId)
                .orElseThrow(() -> new StoreException(StoreErrorStatus._STORE_NOT_FOUND));

        StoreTable storeTable = storeTableRepository.findById(tableId)
                .orElseThrow(() -> new StoreTableException(StoreTableErrorStatus._TABLE_NOT_FOUND));

        StoreTableValidator.validateTableBelongsToStore(storeTable, storeId);

        List<TableBlock> tableBlocks = tableBlockRepository.findByStoreTableAndTargetDate(storeTable, date);
        List<LocalTime> bookedTimeList = bookingRepository.findBookedTimesByTableAndDate(tableId, date);
        Set<LocalTime> bookedTimes = new HashSet<>(bookedTimeList);

        SlotCalculator.SlotCalculationResult result = SlotCalculator.calculateSlots(storeTable, date, tableBlocks, bookedTimes);

        return StoreTableConverter.toSlotListDto(
                result.totalSlotCount(),
                result.availableSlotCount(),
                result.slots()
        );
    }

    @Override
    public StoreTableResDto.TableDetailDto getTableDetail(Long storeId, Long tableId, LocalDate targetDate) {
        storeRepository.findById(storeId)
                .orElseThrow(() -> new StoreException(StoreErrorStatus._STORE_NOT_FOUND));

        StoreTable storeTable = storeTableRepository.findById(tableId)
                .orElseThrow(() -> new StoreTableException(StoreTableErrorStatus._TABLE_NOT_FOUND));

        StoreTableValidator.validateTableBelongsToStore(storeTable, storeId);

        List<TableBlock> tableBlocks = tableBlockRepository.findByStoreTableAndTargetDate(storeTable, targetDate);
        List<LocalTime> bookedTimeList = bookingRepository.findBookedTimesByTableAndDate(tableId, targetDate);
        Set<LocalTime> bookedTimes = new HashSet<>(bookedTimeList);

        SlotCalculator.SlotCalculationResult result = SlotCalculator.calculateSlots(storeTable, targetDate, tableBlocks, bookedTimes);

        return StoreTableConverter.toTableDetailDto(
                storeTable,
                targetDate,
                result.totalSlotCount(),
                result.availableSlotCount()
        );
    }
}
