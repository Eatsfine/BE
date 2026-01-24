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
}
