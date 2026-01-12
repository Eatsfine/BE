package com.eatsfine.eatsfine.domain.booking.service;

import com.eatsfine.eatsfine.domain.booking.dto.response.BookingResponseDTO;
import com.eatsfine.eatsfine.domain.booking.exception.BookingException;
import com.eatsfine.eatsfine.domain.booking.repository.BookingRepository;
import com.eatsfine.eatsfine.domain.booking.status.BookingErrorStatus;
import com.eatsfine.eatsfine.domain.businesshours.entity.BusinessHours;
import com.eatsfine.eatsfine.domain.store.entity.Store;
import com.eatsfine.eatsfine.domain.store.repository.StoreRepository;
import com.eatsfine.eatsfine.domain.storetable.entity.StoreTable;
import com.eatsfine.eatsfine.domain.table_layout.entity.TableLayout;
import com.eatsfine.eatsfine.domain.table_layout.repository.TableLayoutRepository;
import com.eatsfine.eatsfine.global.apiPayload.code.status.ErrorStatus;
import com.eatsfine.eatsfine.global.apiPayload.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingQueryServiceImpl implements BookingQueryService {

    private final BookingRepository bookingRepository;
    private final StoreRepository storeRepository;
    private final TableLayoutRepository tableLayoutRepository;

    @Override
    @Transactional(readOnly = true)
    public BookingResponseDTO.TimeSlotListDTO getAvailableTimeSlots(Long storeId, LocalDate date, Integer partySize, Boolean isSplitAccepted) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new BookingException(BookingErrorStatus._STORE_NOT_FOUND));
        BusinessHours hours = store.getBusinessHoursByDay(date.getDayOfWeek());

        List<LocalTime> availableSlots = new ArrayList<>();
        LocalTime currentTime = hours.getOpenTime();

        while (currentTime.isBefore(hours.getCloseTime())) {

            if (!isDuringBreakTime(hours, currentTime)) {
                List<Long> reservedTableIds = bookingRepository.findReservedTableIds(storeId, date, currentTime);

                List<TableLayout> tableLayouts = store.getTableLayouts();
                TableLayout activeTableLayout = tableLayouts.stream()
                        .filter(TableLayout::isActive).findFirst()
                        .orElseThrow(() -> new GeneralException(ErrorStatus._BAD_REQUEST));

                List<StoreTable> activeTables = activeTableLayout.getTables();

                if (canAccommodate(activeTables, reservedTableIds, partySize, isSplitAccepted)) {
                    availableSlots.add(currentTime);
                }
            }
            currentTime = currentTime.plusMinutes(store.getBookingIntervalMinutes());
        }

        return new BookingResponseDTO.TimeSlotListDTO(availableSlots);
    }

    private boolean canAccommodate(List<StoreTable> allTables, List<Long> reservedTableIds, Integer partySize, Boolean isSplitAccepted) {
        List<StoreTable> freeTables = allTables.stream()
                .filter(t -> !reservedTableIds.contains(t.getId()))
                .toList();

        // 1.단일 테이블 가능한지 체크
        if(freeTables.stream().anyMatch(t-> t.getTableSeats() >= partySize)) return true;

        // 2. 단일 테이블로 안 될 때, 나눠 앉기 동의 했을 경우 합계로 체크
        if (isSplitAccepted) {
            int totalSeats = freeTables.stream().mapToInt(StoreTable::getTableSeats).sum();
            return totalSeats >= partySize;
        }

        return false;
    }

    //브레이크 타임 판별 메서드
    private boolean isDuringBreakTime(BusinessHours hours, LocalTime time) {
        if (hours.getBreakStartTime() == null || hours.getBreakEndTime() == null) {
            return false;
        }
        return !time.isBefore(hours.getBreakStartTime()) && time.isBefore(hours.getBreakEndTime()); 
    }

    @Override
    @Transactional(readOnly = true)
    public BookingResponseDTO.AvailableTableListDTO getAvailableTables(Long storeId, LocalDate date, LocalTime time, Integer partySize,Boolean isSplitAccepted, String seatsType) {
        TableLayout activeTableLayout = tableLayoutRepository.findByStoreIdAndIsActiveTrue(storeId)
                .orElseThrow(() -> new BookingException(BookingErrorStatus._LAYOUT_NOT_FOUND));
        List<Long> reservedTableIds = bookingRepository.findReservedTableIds(storeId, date, time);


        List<BookingResponseDTO.TableInfoDTO> availableTables = activeTableLayout.getTables().stream()
                .filter(t -> !reservedTableIds.contains(t.getId()))
                .filter(t -> {
                    // 사용자가 "분리 허용 안 함(false)"을 선택했다면, 테이블 크기가 인원수보다 커야 함
                    if (isSplitAccepted != null && !isSplitAccepted) {
                        return t.getTableSeats() >= partySize;
                    }
                    // 사용자가 "분리 허용(true)"을 선택했다면 모든 빈 테이블 표시
                    return true;
                })
                .filter(t -> seatsType == null || (t.getSeatsType() != null && t.getSeatsType().name().equalsIgnoreCase(seatsType)))
                .map(t -> BookingResponseDTO.TableInfoDTO.builder()
                        .tableId(t.getId())
                        .tableNumber(t.getTableNumber())
                        .tableSeats(t.getTableSeats())
                        .seatsType(t.getSeatsType().name())
                        .gridX(t.getGridX())
                        .gridY(t.getGridY())
                        .widthSpan(t.getWidthSpan())
                        .heightSpan(t.getHeightSpan())
                        .build())
                .toList();

        return BookingResponseDTO.AvailableTableListDTO.builder()
                .rows(activeTableLayout.getLows())
                .cols(activeTableLayout.getCols())
                .tables(availableTables)
                .build();
    }
}
