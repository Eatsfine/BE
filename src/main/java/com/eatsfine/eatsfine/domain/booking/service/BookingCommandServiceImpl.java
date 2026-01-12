package com.eatsfine.eatsfine.domain.booking.service;

import com.eatsfine.eatsfine.domain.booking.dto.request.BookingRequestDTO;
import com.eatsfine.eatsfine.domain.booking.dto.response.BookingResponseDTO;
import com.eatsfine.eatsfine.domain.booking.entity.Booking;
import com.eatsfine.eatsfine.domain.booking.entity.mapping.BookingTable;
import com.eatsfine.eatsfine.domain.booking.enums.BookingStatus;
import com.eatsfine.eatsfine.domain.booking.exception.BookingException;
import com.eatsfine.eatsfine.domain.booking.repository.BookingRepository;
import com.eatsfine.eatsfine.domain.booking.status.BookingErrorStatus;
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

        int totalDeposit = store.getMinPrice() * dto.partySize();  // 자세한 예약금 로직은 추후 수정


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

        Booking savedBooking = bookingRepository.save(booking);


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

        return BookingResponseDTO.CreateBookingResultDTO.builder()
                .bookingId(savedBooking.getId())
                .storeName(store.getStoreName())
                .date(savedBooking.getBookingDate())
                .time(savedBooking.getBookingTime())
                .partySize(savedBooking.getPartySize())
                .status(savedBooking.getStatus().name())
                .totalDeposit(totalDeposit)
                .createdAt(savedBooking.getCreatedAt())
                .tables(resultTableDTOS)
                .build();
    }
}
