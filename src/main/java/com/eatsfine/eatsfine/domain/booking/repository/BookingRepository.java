package com.eatsfine.eatsfine.domain.booking.repository;

import com.eatsfine.eatsfine.domain.booking.entity.Booking;
import com.eatsfine.eatsfine.domain.booking.enums.BookingStatus;
import com.eatsfine.eatsfine.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {


    @Query("Select bt.storeTable.id from BookingTable bt " +
            "join bt.booking b " +
            "where b.store.id = :storeId " +
            "and b.bookingDate = :date " +
            "and b.bookingTime = :time " +
            "and b.status IN ('CONFIRMED', 'PENDING')")
    List<Long> findReservedTableIds(Long storeId, LocalDate date, LocalTime time);

    @Query("SELECT b.bookingTime FROM BookingTable bt " +
            "JOIN bt.booking b " +
            "WHERE bt.storeTable.id = :tableId " +
            "AND b.bookingDate = :date " +
            "AND b.status IN ('CONFIRMED', 'PENDING')")
    List<LocalTime> findBookedTimesByTableAndDate(@Param("tableId") Long tableId, @Param("date") LocalDate date);

    @Query("SELECT COUNT(bt) > 0 FROM BookingTable bt " +
            "JOIN bt.booking b " +
            "WHERE bt.storeTable.id = :tableId " +
            "AND b.bookingDate = :date " +
            "AND b.bookingTime = :time " +
            "AND b.status IN ('CONFIRMED', 'PENDING')")
    boolean existsBookingByTableAndDateTime(@Param("tableId") Long tableId, @Param("date") LocalDate date, @Param("time") LocalTime time);


    // 1. 특정 유저의 모든 예약을 최신순으로 페이징 조회
    @Query("select b from Booking b join fetch b.store where b.user = :user")
    Page<Booking> findAllByUser(@Param("user") User user, Pageable pageable);

    @Query("Select b from Booking b join fetch b.store where b.user = :user and b.status = :status")
    Page<Booking> findAllByUserAndStatus(@Param("user") User user, @Param("status") BookingStatus status, Pageable pageable);
    @Query("SELECT COUNT(bt) > 0 FROM BookingTable bt " +
            "JOIN bt.booking b " +
            "WHERE bt.storeTable.id = :tableId " +
            "AND (b.bookingDate > :currentDate " +
            "     OR (b.bookingDate = :currentDate AND b.bookingTime >= :currentTime)) " +
            "AND b.status IN ('CONFIRMED', 'PENDING')")
    boolean existsFutureBookingByTable(@Param("tableId") Long tableId, @Param("currentDate") LocalDate currentDate, @Param("currentTime") LocalTime currentTime);

    // BookingRepository.java
    @Query("SELECT b FROM Booking b " +
            "JOIN b.bookingTables bt " +
            "JOIN bt.storeTable st " +
            "WHERE st.id = :tableId " +
            "AND b.bookingDate = :date " +
            "AND b.status = 'CONFIRMED'")
    List<Booking> findActiveBookingsByTableAndDate(
            @Param("tableId") Long tableId,
            @Param("date") LocalDate date);

    Optional<Booking> findByIdAndStatus(Long bookingId, BookingStatus status);

    /**
     * PENDING 상태이면서, 기준 시간(createdAt)보다 이전에 생성된 예약 목록 조회
     * * @param status 예약 상태 (예: PENDING)
     * @param threshold 기준 시간 (예: 현재 시간 - 10분)
     * @return 만료된 예약 리스트
     */
    List<Booking> findAllByStatusAndCreatedAtBefore(BookingStatus status, LocalDateTime threshold);


    // 특정 식당의 특정 시간대와 겹치는 가장 늦은 예약 날짜 찾기
    @Query("select max(b.bookingDate) from Booking b " +
            "where b.store.id = :storeId " +
            "and b.status IN (com.eatsfine.eatsfine.domain.booking.enums.BookingStatus.CONFIRMED, com.eatsfine.eatsfine.domain.booking.enums.BookingStatus.PENDING) " +
            "and b.bookingDate >= CURRENT_DATE " +
            "and (" +
            "   (b.bookingTime >= :breakStart and b.bookingTime < :breakEnd) " + // 케이스 A: 브레이크 중에 예약 시작
            "   OR " +
            "   (b.bookingTime >= :adjustedBreakStart and b.bookingTime < :breakStart)" + // 케이스 B: 브레이크 전에 시작해서 걸침
            ")"
    )
    Optional<LocalDate> findLastConflictingDate(
            @Param("storeId") Long storeId,
            @Param("breakStart") LocalTime breakStart,
            @Param("breakEnd") LocalTime breakEnd,
            @Param("adjustedBreakStart") LocalTime adjustedBreakStart
    );
}
