package com.eatsfine.eatsfine.domain.booking.service;

import com.eatsfine.eatsfine.domain.booking.dto.request.BookingRequestDTO;
import com.eatsfine.eatsfine.domain.booking.dto.response.BookingResponseDTO;
import com.eatsfine.eatsfine.domain.booking.entity.Booking;
import com.eatsfine.eatsfine.domain.booking.enums.BookingStatus;
import com.eatsfine.eatsfine.domain.booking.exception.BookingException;
import com.eatsfine.eatsfine.domain.booking.repository.BookingRepository;
import com.eatsfine.eatsfine.domain.booking.status.BookingErrorStatus;
import com.eatsfine.eatsfine.domain.businesshours.entity.BusinessHours;
import com.eatsfine.eatsfine.domain.payment.entity.Payment;
import com.eatsfine.eatsfine.domain.payment.enums.PaymentStatus;
import com.eatsfine.eatsfine.domain.store.entity.Store;
import com.eatsfine.eatsfine.domain.store.repository.StoreRepository;
import com.eatsfine.eatsfine.domain.store.status.StoreErrorStatus;
import com.eatsfine.eatsfine.domain.storetable.entity.StoreTable;
import com.eatsfine.eatsfine.domain.table_layout.entity.TableLayout;
import com.eatsfine.eatsfine.domain.table_layout.repository.TableLayoutRepository;
import com.eatsfine.eatsfine.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingQueryServiceImpl implements BookingQueryService {

    private final BookingRepository bookingRepository;
    private final StoreRepository storeRepository;
    private final TableLayoutRepository tableLayoutRepository;

    @Override
    @Transactional(readOnly = true)
    public BookingResponseDTO.TimeSlotListDTO getAvailableTimeSlots(Long storeId, BookingRequestDTO.GetAvailableTimeDTO dto) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new BookingException(BookingErrorStatus._STORE_NOT_FOUND));

        BusinessHours hours = store.getBusinessHoursByDay(dto.date().getDayOfWeek());

        if (hours == null) {
            throw new BookingException(StoreErrorStatus._STORE_NOT_OPEN_ON_DAY);
        }

        List<LocalTime> availableSlots = new ArrayList<>();
        LocalTime currentTime = hours.getOpenTime();

        while (currentTime.isBefore(hours.getCloseTime())) {

            if (!isDuringBreakTime(hours, currentTime)) {
                List<Long> reservedTableIds = bookingRepository.findReservedTableIds(storeId, dto.date(), currentTime);

                List<TableLayout> tableLayouts = store.getTableLayouts();
                TableLayout activeTableLayout = tableLayouts.stream()
                        .filter(TableLayout::isActive).findFirst()
                        .orElseThrow(() -> new BookingException(BookingErrorStatus._LAYOUT_NOT_FOUND));

                List<StoreTable> activeTables = activeTableLayout.getTables();

                if (canAccommodate(activeTables, reservedTableIds, dto.partySize(), dto.isSplitAccepted())) {
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
    public BookingResponseDTO.AvailableTableListDTO getAvailableTables(Long storeId, BookingRequestDTO.GetAvailableTableDTO dto) {
        TableLayout activeTableLayout = tableLayoutRepository.findByStoreIdAndIsActiveTrue(storeId)
                .orElseThrow(() -> new BookingException(BookingErrorStatus._LAYOUT_NOT_FOUND));
        List<Long> reservedTableIds = bookingRepository.findReservedTableIds(storeId, dto.date(), dto.time());


        List<BookingResponseDTO.TableInfoDTO> availableTables = activeTableLayout.getTables().stream()
                .filter(t -> !reservedTableIds.contains(t.getId()))
                .filter(t -> {
                    // 사용자가 "분리 허용 안 함(false)"을 선택했다면, 테이블 크기가 인원수보다 커야 함
                    if (dto.isSplitAccepted() != null && !dto.isSplitAccepted()) {
                        return t.getTableSeats() >= dto.partySize();
                    }
                    // 사용자가 "분리 허용(true)"을 선택했다면 모든 빈 테이블 표시
                    return true;
                })
                .filter(t -> dto.seatsType() == null || dto.seatsType().isEmpty() || (t.getSeatsType() != null && t.getSeatsType().name().equalsIgnoreCase(dto.seatsType())))
                .map(t -> BookingResponseDTO.TableInfoDTO.builder()
                        .tableId(t.getId())
                        .tableNumber(t.getTableNumber())
                        .tableSeats(t.getTableSeats())
                        .seatsType(t.getSeatsType() != null ? t.getSeatsType().name() : null)
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

    @Override
    public BookingResponseDTO.BookingPreviewListDTO getBookingList(User user, String status, Integer page) {
        PageRequest pageRequest = PageRequest.of(page, 10, Sort.by("bookingDate").descending());

        Page<Booking> bookingPage;

        if(status == null || status.equals("ALL")) {
            bookingPage = bookingRepository.findAllByUser(user, pageRequest);
        } else {
            BookingStatus bookingStatus = BookingStatus.valueOf(status);
            bookingPage = bookingRepository.findAllByUserAndStatus(user, bookingStatus, pageRequest);
        }

        List<BookingResponseDTO.BookingPreviewDTO> bookingPreviewDTOList = bookingPage.getContent().stream()
                .map(booking -> {

                    // 성공한 결제 정보 추출 (1:N 대응)
                    Payment successPayment = booking.getPayments().stream()
                            .filter(p -> p.getPaymentStatus() == PaymentStatus.COMPLETED || p.getPaymentStatus() == PaymentStatus.REFUNDED)
                            .findFirst()
                            .orElse(null);

                    // 테이블 번호들을 하나의 문자열로 합치기
                    String tableNumbers = booking.getBookingTables().stream()
                            .map(bt -> bt.getStoreTable().getTableNumber().toString())
                            .collect(Collectors.joining(", "));

                    return BookingResponseDTO.BookingPreviewDTO.builder()
                            .bookingId(booking.getId())
                            .storeName(booking.getStore().getStoreName())
                            .storeAddress(booking.getStore().getAddress())
                            .bookingDate(booking.getBookingDate())
                            .bookingTime(booking.getBookingTime())
                            .partySize(booking.getPartySize())
                            .tableNumbers(tableNumbers + "번")
                            .amount(successPayment != null ? successPayment.getAmount() : booking.getDepositAmount())
                            .paymentMethod(successPayment != null ? successPayment.getPaymentMethod().name() : "미결제")
                            .status(booking.getStatus().name())
                            .build();
                }).collect(Collectors.toList());

        return BookingResponseDTO.BookingPreviewListDTO.builder()
                .isLast(bookingPage.isLast())
                .isFirst(bookingPage.isFirst())
                .totalPage(bookingPage.getTotalPages())
                .totalElements(bookingPage.getTotalElements())
                .listSize(bookingPreviewDTOList.size())
                .bookingList(bookingPreviewDTOList)
                .build();
    }
}
