package com.eatsfine.eatsfine.domain.tableblock.service;

import com.eatsfine.eatsfine.domain.booking.repository.BookingRepository;
import com.eatsfine.eatsfine.domain.store.entity.Store;
import com.eatsfine.eatsfine.domain.store.exception.StoreException;
import com.eatsfine.eatsfine.domain.store.repository.StoreRepository;
import com.eatsfine.eatsfine.domain.store.status.StoreErrorStatus;
import com.eatsfine.eatsfine.domain.store.validator.StoreValidator;
import com.eatsfine.eatsfine.domain.storetable.entity.StoreTable;
import com.eatsfine.eatsfine.domain.storetable.exception.StoreTableException;
import com.eatsfine.eatsfine.domain.storetable.exception.status.StoreTableErrorStatus;
import com.eatsfine.eatsfine.domain.storetable.repository.StoreTableRepository;
import com.eatsfine.eatsfine.domain.storetable.validator.StoreTableValidator;
import com.eatsfine.eatsfine.domain.tableblock.converter.TableBlockConverter;
import com.eatsfine.eatsfine.domain.tableblock.dto.req.TableBlockReqDto;
import com.eatsfine.eatsfine.domain.tableblock.dto.res.TableBlockResDto;
import com.eatsfine.eatsfine.domain.tableblock.entity.TableBlock;
import com.eatsfine.eatsfine.domain.tableblock.enums.SlotStatus;
import com.eatsfine.eatsfine.domain.tableblock.exception.TableBlockException;
import com.eatsfine.eatsfine.domain.tableblock.exception.status.TableBlockErrorStatus;
import com.eatsfine.eatsfine.domain.tableblock.repository.TableBlockRepository;
import com.eatsfine.eatsfine.domain.tableblock.validator.TableBlockValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class TableBlockCommandServiceImpl implements TableBlockCommandService {
    private final StoreTableRepository storeTableRepository;
    private final TableBlockRepository tableBlockRepository;
    private final StoreRepository storeRepository;
    private final BookingRepository bookingRepository;
    private final StoreValidator storeValidator;

    // 테이블 슬롯 상태 변경
    @Override
    public TableBlockResDto.SlotStatusUpdateDto updateSlotStatus(
            Long storeId,
            Long tableId,
            TableBlockReqDto.SlotStatusUpdateDto dto,
            String email
            ) {

        Store store = storeValidator.validateStoreOwner(storeId, email);

        StoreTable table = storeTableRepository.findById(tableId)
                .orElseThrow(() -> new StoreTableException(StoreTableErrorStatus._TABLE_NOT_FOUND));

        StoreTableValidator.validateTableBelongsToStore(table, storeId);

        // 브레이크 타임 시간대라면 예외
        TableBlockValidator.validateBreakTime(store, dto.targetDate(), dto.startTime());

        // 예약 여부 조회
        boolean isBooked = bookingRepository.existsBookingByTableAndDateTime(tableId, dto.targetDate(), dto.startTime());

        // 슬롯 차단
        if (dto.status() == SlotStatus.BLOCKED) {
            TableBlockValidator.validateBlockBooking(isBooked);

            Optional<TableBlock> existingBlock = tableBlockRepository
                    .findByStoreTableAndTargetDateAndStartTime(table, dto.targetDate(), dto.startTime());

            if (existingBlock.isPresent()) {
                return TableBlockConverter.toSlotStatusUpdateDto(existingBlock.get(), SlotStatus.BLOCKED);
            }

            LocalTime endTime = dto.startTime().plusMinutes(store.getBookingIntervalMinutes());

            TableBlock tableBlock = TableBlock.builder()
                    .storeTable(table)
                    .targetDate(dto.targetDate())
                    .startTime(dto.startTime())
                    .endTime(endTime)
                    .build();

            TableBlock savedBlock = tableBlockRepository.save(tableBlock);

            return TableBlockConverter.toSlotStatusUpdateDto(savedBlock, SlotStatus.BLOCKED);
        }

        // 슬롯 차단 해제
        if (dto.status() == SlotStatus.AVAILABLE) {
            TableBlockValidator.validateUnblockBooking(isBooked);

            TableBlock tableBlock = tableBlockRepository
                    .findByStoreTableAndTargetDateAndStartTime(table, dto.targetDate(), dto.startTime())
                    .orElseThrow(() -> new TableBlockException(TableBlockErrorStatus._TABLE_BLOCK_NOT_FOUND));

            tableBlockRepository.delete(tableBlock);

            return TableBlockConverter.toSlotStatusUpdateDto(tableBlock, SlotStatus.AVAILABLE);
        }

        throw new TableBlockException(TableBlockErrorStatus._INVALID_SLOT_STATUS);
    }
}
