package com.eatsfine.eatsfine.domain.businesshours.repository;

import com.eatsfine.eatsfine.domain.businesshours.entity.BusinessHours;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BusinessHoursRepository extends JpaRepository<BusinessHours,Long> {
}
