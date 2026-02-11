package com.eatsfine.eatsfine.domain.businesshours.repository;

import com.eatsfine.eatsfine.domain.businesshours.entity.BusinessHours;
import com.eatsfine.eatsfine.domain.store.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface BusinessHoursRepository extends JpaRepository<BusinessHours, Long> {
    Optional<BusinessHours> findByStoreAndDayOfWeek(Store store, DayOfWeek dayOfWeek);
    List<BusinessHours> findAllByEffectiveDate(LocalDate date);
}
