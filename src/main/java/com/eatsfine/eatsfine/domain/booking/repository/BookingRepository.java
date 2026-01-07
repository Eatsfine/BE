package com.eatsfine.eatsfine.domain.booking.repository;

import com.eatsfine.eatsfine.domain.booking.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingRepository extends JpaRepository<Booking,Long> {
}
