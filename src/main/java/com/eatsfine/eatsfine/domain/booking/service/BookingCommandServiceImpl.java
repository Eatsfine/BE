package com.eatsfine.eatsfine.domain.booking.service;

import com.eatsfine.eatsfine.domain.booking.converter.BookingConverter;
import com.eatsfine.eatsfine.domain.booking.dto.request.BookingRequestDTO;
import com.eatsfine.eatsfine.domain.booking.dto.response.BookingResponseDTO;
import com.eatsfine.eatsfine.domain.booking.entity.Booking;
import com.eatsfine.eatsfine.domain.booking.entity.mapping.BookingTable;
import com.eatsfine.eatsfine.domain.booking.enums.BookingStatus;
import com.eatsfine.eatsfine.domain.booking.exception.BookingException;
import com.eatsfine.eatsfine.domain.booking.repository.BookingRepository;
import com.eatsfine.eatsfine.domain.booking.status.BookingErrorStatus;
import com.eatsfine.eatsfine.domain.payment.dto.request.PaymentRequestDTO;
import com.eatsfine.eatsfine.domain.payment.dto.response.PaymentResponseDTO;
import com.eatsfine.eatsfine.domain.payment.service.PaymentService;
import com.eatsfine.eatsfine.domain.store.entity.Store;
import com.eatsfine.eatsfine.domain.store.exception.StoreException;
import com.eatsfine.eatsfine.domain.store.repository.StoreRepository;
import com.eatsfine.eatsfine.domain.store.status.StoreErrorStatus;
import com.eatsfine.eatsfine.domain.storetable.entity.StoreTable;
import com.eatsfine.eatsfine.domain.storetable.repository.StoreTableRepository;
import com.eatsfine.eatsfine.domain.user.entity.User;
import com.eatsfine.eatsfine.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class BookingCommandServiceImpl implements BookingCommandService{

    private final StoreRepository storeRepository;
    private final StoreTableRepository storeTableRepository;
    private final BookingRepository bookingRepository;
    private final PaymentService paymentService;

    @Override
    @Transactional
    public BookingResponseDTO.CreateBookingResultDTO createBooking(User user, Long storeId, BookingRequestDTO.CreateBookingDTO dto) {

        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new StoreException(StoreErrorStatus._STORE_NOT_FOUND));

        List<StoreTable> selectedTables = storeTableRepository.findAllByIdWithLock(dto.tableIds());

        //이미 예약된 테이블 있는지 최종 점검
        List<Long> reservedTableIds = bookingRepository.findReservedTableIds(storeId, dto.date(), dto.time());
        for (StoreTable storeTable : selectedTables) {
            if (reservedTableIds.contains(storeTable.getId())) {
                throw new BookingException(BookingErrorStatus._ALREADY_RESERVED_TABLE);
            }
        }

        int totalDeposit =  store.getMinPrice() * store.getDepositRate().getPercent() / 100;  // 자세한 예약금 로직은 추후 수정


        Booking booking = Booking.builder()
                .depositAmount(totalDeposit)
                .bookingDate(dto.date())
                .bookingTime(dto.time())
                .partySize(dto.partySize())
                .status(BookingStatus.PENDING)
                .store(store)
                .user(user)
                .isSplitAccepted(dto.isSplitAccepted())
                .build();

        selectedTables.forEach(booking::addBookingTable);

        Booking savedBooking = bookingRepository.save(booking);

        // 결제 대기 데이터 생성 (내부 서비스 호출)
        PaymentRequestDTO.RequestPaymentDTO paymentRequest = new PaymentRequestDTO.RequestPaymentDTO(savedBooking.getId());
        PaymentResponseDTO.PaymentRequestResultDTO paymentInfo = paymentService.requestPayment(paymentRequest);


        //BookingResponseDTO.BookingResultTableDTO로 변환
        List<BookingResponseDTO.BookingResultTableDTO> resultTableDTOS = savedBooking.getBookingTables().stream()
                .map(BookingTable::getStoreTable)
                .map(t -> BookingResponseDTO.BookingResultTableDTO.builder()
                        .tableId(t.getId())
                        .tableNumber(t.getTableNumber())
                        .tableSeats(t.getTableSeats())
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
    public BookingResponseDTO.CancelBookingResultDTO cancelBooking(Long bookingId, BookingRequestDTO.CancelBookingDTO dto) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingException(BookingErrorStatus._BOOKING_NOT_FOUND));


        // 이미 취소된 예약인지 최종 확인
        if(booking.getStatus().equals(BookingStatus.CANCELED)) {
            throw new BookingException(BookingErrorStatus._ALREADY_CONFIRMED);
        }

        // TODO 환불 로직


        //예약 상태 변경
        booking.cancel(dto.reason());

        return BookingResponseDTO.CancelBookingResultDTO.builder()
                .bookingId(booking.getId())
                .status(booking.getStatus().name())
                .refundAmount(booking.getDepositAmount())
                .build();
    }
}
