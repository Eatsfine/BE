package com.eatsfine.eatsfine.domain.booking.service;

import com.eatsfine.eatsfine.domain.booking.dto.request.BookingRequestDTO;
import com.eatsfine.eatsfine.domain.booking.dto.response.BookingResponseDTO;
import com.eatsfine.eatsfine.domain.booking.entity.Booking;
import com.eatsfine.eatsfine.domain.booking.enums.BookingStatus;
import com.eatsfine.eatsfine.domain.booking.exception.BookingException;
import com.eatsfine.eatsfine.domain.booking.repository.BookingRepository;
import com.eatsfine.eatsfine.domain.booking.status.BookingErrorStatus;
import com.eatsfine.eatsfine.domain.businesshours.entity.BusinessHours;
import com.eatsfine.eatsfine.domain.businesshours.exception.BusinessHoursException;
import com.eatsfine.eatsfine.domain.businesshours.repository.BusinessHoursRepository;
import com.eatsfine.eatsfine.domain.businesshours.status.BusinessHoursErrorStatus;
import com.eatsfine.eatsfine.domain.payment.entity.Payment;
import com.eatsfine.eatsfine.domain.payment.enums.PaymentStatus;
import com.eatsfine.eatsfine.domain.store.entity.Store;
import com.eatsfine.eatsfine.domain.store.repository.StoreRepository;
import com.eatsfine.eatsfine.domain.store.status.StoreErrorStatus;
import com.eatsfine.eatsfine.domain.storetable.entity.StoreTable;
import com.eatsfine.eatsfine.domain.storetable.repository.StoreTableRepository;
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
    private final BusinessHoursRepository businessHourRepository;
    private final StoreTableRepository storeTableRepository;

    @Override
    @Transactional(readOnly = true)
    public BookingResponseDTO.TimeSlotListDTO getAvailableTimeSlots(Long storeId, BookingRequestDTO.GetAvailableTimeDTO dto) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new BookingException(BookingErrorStatus._STORE_NOT_FOUND));

        // 1. 영업시간 조회
        BusinessHours hours = businessHourRepository.findByStoreAndDayOfWeek(store, dto.date().getDayOfWeek())
                .orElseThrow(() -> new BusinessHoursException(BusinessHoursErrorStatus._BUSINESS_HOURS_DAY_NOT_FOUND));

        // 2. 루프 밖에서 활성 레이아웃과 테이블 리스트를 DB에서 직접 조회
        TableLayout activeLayout = tableLayoutRepository.findByStoreIdAndIsActiveTrue(store.getId())
                .orElseThrow(() -> new BookingException(BookingErrorStatus._LAYOUT_NOT_FOUND));

        List<StoreTable> activeTables = storeTableRepository.findAllByTableLayoutAndIsDeletedFalse(activeLayout);

        // 테이블 정보가 아예 없는 경우 검증
        if (activeTables.isEmpty()) {
            throw new BookingException(BookingErrorStatus._TABLE_NOT_FOUND);
        }

        List<LocalTime> availableSlots = new ArrayList<>();
        LocalTime currentTime = hours.getOpenTime();

        while (currentTime.isBefore(hours.getCloseTime())) {
            if (!isDuringBreakTime(hours, currentTime)) {
                // 해당 시간대의 예약된 테이블 ID 목록만 DB에서 조회
                List<Long> reservedTableIds = bookingRepository.findReservedTableIds(storeId, dto.date(), currentTime);

                // 미리 가져온 activeTables를 사용하여 검증 (테이블 seats가 null인 경우 등은 canAccommodate 내부에서 처리 권장)
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

        // 1. 단일 테이블 범위 내 수용 가능한지 체크
        if (freeTables.stream().anyMatch(t ->
                t.getMinSeatCount() != null && t.getMaxSeatCount() != null &&
                        partySize >= t.getMinSeatCount() && partySize <= t.getMaxSeatCount())) {
            return true;
        }

        // 2. 나눠 앉기 시 합계 체크 (최소 인원 합산 조건 등은 비즈니스에 따라 추가 가능)
        if (Boolean.TRUE.equals(isSplitAccepted)) {
            int totalMaxSeats = freeTables.stream()
                    .mapToInt(t -> t.getMaxSeatCount() != null ? t.getMaxSeatCount() : 0)
                    .sum();
            return totalMaxSeats >= partySize;
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

                    if (t.getMinSeatCount() == null || t.getMaxSeatCount() == null) {
                        throw new BookingException(BookingErrorStatus._TABLE_SEATS_NOT_FOUND);
                    }

                    // 1. 공통 조건: 예약 인원이 테이블의 최소 인원보다 크거나 같고, 최대 인원보다 작거나 같아야 함
                    boolean isWithinRange = dto.partySize() >= t.getMinSeatCount() && dto.partySize() <= t.getMaxSeatCount();

                    // 2. "나눠 앉기 비허용" 시에는 단일 테이블의 수용 범위가 정확히 맞아야 함
                    if (Boolean.FALSE.equals(dto.isSplitAccepted())) {
                        return isWithinRange;
                    }

                    // 3. "나눠 앉기 허용" 시에는 예약 인원이 테이블의 최소 인원보다는 많아야 선택 가능 (최대 인원은 초과해도 됨)
                    // (여러 테이블을 합칠 예정이므로 최소 인원 조건만 충족하면 표시해 줍니다.)
                    return dto.partySize() >= t.getMinSeatCount();
                })
                .filter(t -> dto.seatsType() == null || dto.seatsType().isEmpty() ||
                        (t.getSeatsType() != null && t.getSeatsType().name().equalsIgnoreCase(dto.seatsType())))
                .map(t -> BookingResponseDTO.TableInfoDTO.builder()
                        .tableId(t.getId())
                        .tableNumber(t.getTableNumber())
                        .tableSeats(t.getMaxSeatCount())
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

    // 사장님용 예약 상세 조회
    @Override
    @Transactional(readOnly = true)
    public BookingResponseDTO.BookingDetailDTO getBookingDetail(Long storeId, Long tableId, Long bookingId) {
        // 1. 예약 존재 여부 확인
        Booking booking = bookingRepository.findByIdAndStatus(bookingId, BookingStatus.CONFIRMED)
                .orElseThrow(() -> new BookingException(BookingErrorStatus._BOOKING_NOT_FOUND));

        // 2. 계층 구조를 통한 데이터 무결성 검증
        // Booking에 연결된 BookingTable 리스트에서 해당 tableId가 있는지 확인
        boolean isCorrectTable = booking.getBookingTables().stream()
                .anyMatch(bt -> bt.getStoreTable().getId().equals(tableId));

        // 해당 테이블이 속한 가게 ID가 요청받은 storeId와 일치하는지 확인
        boolean isCorrectStore = booking.getStore().getId().equals(storeId);

        if (!isCorrectTable || !isCorrectStore) {
            // "잘못된 접근입니다" 또는 "해당 가게의 예약이 아닙니다" 예외 발생
            throw new BookingException(BookingErrorStatus._INVALID_BOOKING_ACCESS);
        }

        return BookingResponseDTO.BookingDetailDTO.builder()
                .bookerName(booking.getUser().getName())
                .partySize(booking.getPartySize())
                .amount(booking.getDepositAmount())
                .build();
    }
}
