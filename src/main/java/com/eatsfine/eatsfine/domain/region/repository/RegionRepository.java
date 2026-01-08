package com.eatsfine.eatsfine.domain.region.repository;

import com.eatsfine.eatsfine.domain.region.entity.Region;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RegionRepository extends JpaRepository<Region, Long> {
}
