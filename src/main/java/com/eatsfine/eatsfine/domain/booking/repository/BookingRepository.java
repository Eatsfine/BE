package com.eatsfine.eatsfine.domain.booking.repository;

import com.eatsfine.eatsfine.domain.booking.entity.Booking;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

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

    @Query("SELECT COUNT(bt) > 0 FROM BookingTable bt " +
            "JOIN bt.booking b " +
            "WHERE bt.storeTable.id = :tableId " +
            "AND (b.bookingDate > :currentDate " +
            "     OR (b.bookingDate = :currentDate AND b.bookingTime >= :currentTime)) " +
            "AND b.status IN ('CONFIRMED', 'PENDING')")
    boolean existsFutureBookingByTable(@Param("tableId") Long tableId, @Param("currentDate") LocalDate currentDate, @Param("currentTime") LocalTime currentTime);
}
