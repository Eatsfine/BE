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
import java.time.LocalTime;
import java.util.List;

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
}
