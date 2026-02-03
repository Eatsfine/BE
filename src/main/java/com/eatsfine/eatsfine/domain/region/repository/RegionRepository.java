package com.eatsfine.eatsfine.domain.region.repository;

import com.eatsfine.eatsfine.domain.region.entity.Region;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RegionRepository extends JpaRepository<Region, Long> {
    Optional<Region> findBySidoAndSigunguAndBname(String sido, String sigungu, String bname);
}
