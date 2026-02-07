package com.eatsfine.eatsfine.domain.booking.service;

import com.eatsfine.eatsfine.domain.booking.converter.BookingConverter;
import com.eatsfine.eatsfine.domain.booking.dto.request.BookingRequestDTO;
import com.eatsfine.eatsfine.domain.booking.dto.response.BookingResponseDTO;
import com.eatsfine.eatsfine.domain.booking.entity.Booking;
import com.eatsfine.eatsfine.domain.booking.entity.mapping.BookingMenu;
import com.eatsfine.eatsfine.domain.booking.entity.mapping.BookingTable;
import com.eatsfine.eatsfine.domain.booking.enums.BookingStatus;
import com.eatsfine.eatsfine.domain.booking.exception.BookingException;
import com.eatsfine.eatsfine.domain.booking.repository.BookingRepository;
import com.eatsfine.eatsfine.domain.booking.status.BookingErrorStatus;
import com.eatsfine.eatsfine.domain.menu.entity.Menu;
import com.eatsfine.eatsfine.domain.menu.repository.MenuRepository;
import com.eatsfine.eatsfine.domain.payment.dto.request.PaymentRequestDTO;
import com.eatsfine.eatsfine.domain.payment.dto.response.PaymentResponseDTO;
import com.eatsfine.eatsfine.domain.payment.entity.Payment;
import com.eatsfine.eatsfine.domain.payment.enums.PaymentStatus;
import com.eatsfine.eatsfine.domain.payment.exception.PaymentException;
import com.eatsfine.eatsfine.domain.payment.service.PaymentService;
import com.eatsfine.eatsfine.domain.payment.status.PaymentErrorStatus;
import com.eatsfine.eatsfine.domain.store.entity.Store;
import com.eatsfine.eatsfine.domain.store.exception.StoreException;
import com.eatsfine.eatsfine.domain.store.repository.StoreRepository;
import com.eatsfine.eatsfine.domain.store.status.StoreErrorStatus;
import com.eatsfine.eatsfine.domain.storetable.entity.StoreTable;
import com.eatsfine.eatsfine.domain.storetable.exception.status.StoreTableErrorStatus;
import com.eatsfine.eatsfine.domain.storetable.repository.StoreTableRepository;
import com.eatsfine.eatsfine.domain.user.entity.User;
import com.eatsfine.eatsfine.domain.user.exception.UserException;
import com.eatsfine.eatsfine.domain.user.repository.UserRepository;
import com.eatsfine.eatsfine.domain.user.status.UserErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class BookingCommandServiceImpl implements BookingCommandService{

    private final StoreRepository storeRepository;
    private final StoreTableRepository storeTableRepository;
    private final BookingRepository bookingRepository;
    private final PaymentService paymentService;
    private final MenuRepository menuRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public BookingResponseDTO.CreateBookingResultDTO createBooking(Long userId, Long storeId, BookingRequestDTO.CreateBookingDTO dto) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(UserErrorStatus.MEMBER_NOT_FOUND));

        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new StoreException(StoreErrorStatus._STORE_NOT_FOUND));

        List<StoreTable> selectedTables = storeTableRepository.findAllByIdWithLock(dto.tableIds());

        // 요청한 ID 개수와 조회된 데이터 개수가 다르면, 존재하지 않는 ID가 포함된 것
        if (selectedTables.size() != dto.tableIds().size()) {
            throw new StoreException(StoreTableErrorStatus._TABLE_NOT_FOUND);
        }

        //이미 예약된 테이블 있는지 최종 점검
        List<Long> reservedTableIds = bookingRepository.findReservedTableIds(storeId, dto.date(), dto.time());
        for (StoreTable storeTable : selectedTables) {
            if (reservedTableIds.contains(storeTable.getId())) {
                throw new BookingException(BookingErrorStatus._ALREADY_RESERVED_TABLE);
            }
        }


        Booking booking = Booking.builder()
                .bookingDate(dto.date())
                .bookingTime(dto.time())
                .partySize(dto.partySize())
                .status(BookingStatus.PENDING)
                .store(store)
                .user(user)
                .isSplitAccepted(dto.isSplitAccepted())
                .build();

        selectedTables.forEach(booking::addBookingTable);


        // 예약한 메뉴들 저장 및 총 메뉴 가격 계산
        BigDecimal itemTotalPrice = BigDecimal.ZERO;
        for (BookingRequestDTO.MenuOrderDto menuItem : dto.menuItems()) {
            Menu menu = menuRepository.findById(menuItem.menuId())
                    .orElseThrow(() -> new StoreException(StoreErrorStatus._STORE_NOT_FOUND));//차후 수정

            BookingMenu bookingMenu = BookingMenu.builder()
                    .quantity(menuItem.quantity())
                    .menu(menu)
                    .booking(booking)
                    .price(menu.getPrice())
                    .build();

            booking.addBookingMenu(bookingMenu);

            BigDecimal itemQuantity = BigDecimal.valueOf(menuItem.quantity());
            itemTotalPrice = itemTotalPrice.add(menu.getPrice().multiply(itemQuantity));
        }

        // 총 예약금 계산 ( 전체 메뉴 가격 * 가게의 예약금 비율 )
        BigDecimal depositRate = BigDecimal.valueOf(store.getDepositRate().getPercent());
        BigDecimal hundred = BigDecimal.valueOf(100);
        BigDecimal totalDeposit = itemTotalPrice
                .multiply(depositRate)
                .divide(hundred, 0, RoundingMode.HALF_UP);
        booking.setDepositAmount(totalDeposit);

        Booking savedBooking = bookingRepository.save(booking);
        bookingRepository.flush();

        // 결제 대기 데이터 생성 (내부 서비스 호출)
        PaymentRequestDTO.RequestPaymentDTO paymentRequest = new PaymentRequestDTO.RequestPaymentDTO(savedBooking.getId());
        PaymentResponseDTO.PaymentRequestResultDTO paymentInfo = paymentService.requestPayment(paymentRequest);


        //BookingResponseDTO.BookingResultTableDTO로 변환
        List<BookingResponseDTO.BookingResultTableDTO> resultTableDTOS = savedBooking.getBookingTables().stream()
                .map(BookingTable::getStoreTable)
                .map(t -> BookingResponseDTO.BookingResultTableDTO.builder()
                        .tableId(t.getId())
                        .tableNumber(t.getTableNumber())
                        .tableSeats(t.getMaxSeatCount())
                        .seatsType(t.getSeatsType() != null ? t.getSeatsType().name() : null)
                        .build())
                .toList();


        return BookingConverter.toCreateBookingResultDTO(savedBooking,store,totalDeposit, resultTableDTOS,paymentInfo);
    }

    @Override
    @Transactional
    public BookingResponseDTO.ConfirmPaymentResultDTO confirmPayment(Long bookingId, BookingRequestDTO.PaymentConfirmDTO dto) {

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingException(BookingErrorStatus._BOOKING_NOT_FOUND));

        //이미 예약이 확정됐는지 최종 확인
        if(booking.getStatus() == BookingStatus.CONFIRMED) {
            throw new BookingException(BookingErrorStatus._ALREADY_CONFIRMED);
        }

        // 예약 생성 시 설정된 예약금액과 결제 완료된 금액이 일치하는지 확인
        if(!booking.getDepositAmount().equals(dto.amount())) {
            throw new BookingException(BookingErrorStatus._PAYMENT_AMOUNT_MISMATCH);
        }

        //예약 상태 확정으로 변경
        booking.confirm();

        return BookingResponseDTO.ConfirmPaymentResultDTO.builder()
                .bookingId(booking.getId())
                .status(booking.getStatus().name())
                .paymentKey(dto.paymentKey())
                .amount(booking.getDepositAmount())
                .build();
    }

    @Override
    @Transactional
    public BookingResponseDTO.CancelBookingResultDTO cancelBooking(Long userId, Long bookingId, BookingRequestDTO.CancelBookingDTO dto) {


        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingException(BookingErrorStatus._BOOKING_NOT_FOUND));


        //  본인 예약인지 확인
        if (!booking.getUser().getId().equals(userId)) {
            throw new BookingException(BookingErrorStatus._BOOKING_NOT_USER); // 본인 예약이 아님 에러
        }

        //  이미 취소된 예약인지 확인
        if (booking.getStatus() == BookingStatus.CANCELED) {
            throw new BookingException(BookingErrorStatus._ALREADY_CANCELED);
        }

        // 예약 중 결제 완료된 결제의 결제키 이용 환불 로직 진행
        if (booking.getStatus() == BookingStatus.CONFIRMED) {
            PaymentRequestDTO.CancelPaymentDTO cancelDto = new PaymentRequestDTO.CancelPaymentDTO(dto.reason());
            paymentService.cancelPayment(booking.getSuccessPaymentKey(), cancelDto);
        }

        //예약 상태 취소로 변경
        booking.cancel(dto.reason());

        return BookingResponseDTO.CancelBookingResultDTO.builder()
                .bookingId(booking.getId())
                .status(booking.getStatus().name())
                .refundAmount(booking.getDepositAmount())
                .build();
    }

    @Override
    @Transactional
    public BookingResponseDTO.OwnerCancelBookingResultDTO cancelBookingByOwner(Long storeId, Long tableId, Long bookingId) {
        // 1. 예약 존재 확인
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingException(BookingErrorStatus._BOOKING_NOT_FOUND));

        // 2. 데이터 무결성 검증
        // - 해당 예약이 요청된 가게의 예약인지 확인
        if (!booking.getStore().getId().equals(storeId)) {
            throw new BookingException(BookingErrorStatus._INVALID_BOOKING_ACCESS);
        }

        // - 해당 예약의 테이블 목록중에 요청된 tableId가 있는지 확인
        // 정영님의 구조: Booking -> BookingTable -> StoreTable
        boolean isCorrectTable = booking.getBookingTables().stream()
                .anyMatch(bt -> bt.getStoreTable().getId().equals(tableId));

        if (!isCorrectTable) {
            throw new BookingException(BookingErrorStatus._TABLE_NOT_FOUND);
        }

        if (booking.getStatus() == BookingStatus.CANCELED) {
            throw new BookingException(BookingErrorStatus._ALREADY_CANCELED);
        }

        booking.cancel("사장님에 의한 예약 취소");

        // 3. 응답 DTO 반환
        return BookingResponseDTO.OwnerCancelBookingResultDTO.builder()
                .bookingId(booking.getId())
                .status(booking.getStatus().name())
                .canceledAt(LocalDateTime.now())
                //.refundAmount()
                .build();
    }
}
