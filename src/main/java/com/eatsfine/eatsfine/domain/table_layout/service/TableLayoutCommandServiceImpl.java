package com.eatsfine.eatsfine.domain.table_layout.service;

import com.eatsfine.eatsfine.domain.booking.repository.BookingRepository;
import com.eatsfine.eatsfine.domain.store.entity.Store;
import com.eatsfine.eatsfine.domain.store.validator.StoreValidator;
import com.eatsfine.eatsfine.domain.table_layout.converter.TableLayoutConverter;
import com.eatsfine.eatsfine.domain.table_layout.dto.req.TableLayoutReqDto;
import com.eatsfine.eatsfine.domain.table_layout.dto.res.TableLayoutResDto;
import com.eatsfine.eatsfine.domain.table_layout.entity.TableLayout;
import com.eatsfine.eatsfine.domain.table_layout.exception.status.TableLayoutErrorStatus;
import com.eatsfine.eatsfine.domain.table_layout.repository.TableLayoutRepository;
import com.eatsfine.eatsfine.domain.tableblock.exception.TableBlockException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class TableLayoutCommandServiceImpl implements TableLayoutCommandService {
    private final TableLayoutRepository tableLayoutRepository;
    private final BookingRepository bookingRepository;
    private final StoreValidator storeValidator;

    // 테이블 배치도 생성
    @Override
    public TableLayoutResDto.LayoutDetailDto createLayout(
            Long storeId,
            TableLayoutReqDto.LayoutCreateDto dto,
            String email
            ) {

        Store store = storeValidator.validateStoreOwner(storeId, email);

        Optional<TableLayout> existingLayout = tableLayoutRepository.findByStoreIdAndIsActiveTrue(storeId);

        if (existingLayout.isPresent()) {
            // 미래 예약 확인
            boolean hasFutureBookings = checkFutureBookingsInLayout(existingLayout.get());

            if (hasFutureBookings) {
                throw new TableBlockException(TableLayoutErrorStatus._CANNOT_DELETE_LAYOUT_WITH_FUTURE_BOOKINGS);
            }
            
            // 미래 예약이 없으면 배치도 재생성
            deactivateExistingLayout(store);
        }

        // 새 배치도 생성
        TableLayout newLayout = TableLayout.builder()
                .store(store)
                .lows(dto.gridRow())
                .cols(dto.gridCol())
                .isActive(true)
                .isDeleted(false)
                .build();

        TableLayout savedLayout = tableLayoutRepository.save(newLayout);

        return TableLayoutConverter.toLayoutDetailDto(savedLayout);
    }

    // 기존 테이블 배치도 비활성화
    private void deactivateExistingLayout(Store store) {
        tableLayoutRepository.findByStoreIdAndIsActiveTrue(store.getId())
                .ifPresent(tableLayoutRepository::delete);
    }

    // 미래 예약 확인
    private boolean checkFutureBookingsInLayout(TableLayout layout) {
        LocalDate currentDate = LocalDate.now();
        LocalTime currentTime = LocalTime.now();

        return layout.getTables().stream()
                .anyMatch(table ->
                        bookingRepository.existsFutureBookingByTable(table.getId(), currentDate, currentTime)
                );
    }
}
