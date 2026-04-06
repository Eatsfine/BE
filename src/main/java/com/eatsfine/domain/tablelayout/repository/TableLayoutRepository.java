package com.eatsfine.domain.tablelayout.repository;

import com.eatsfine.domain.tablelayout.entity.TableLayout;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TableLayoutRepository extends JpaRepository<TableLayout, Long> {

    Optional<TableLayout> findByStoreIdAndIsActiveTrue(Long storeId);
}
