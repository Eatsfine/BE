package com.eatsfine.eatsfine.domain.table_layout.repository;

import com.eatsfine.eatsfine.domain.table_layout.entity.TableLayout;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TableLayoutRepository extends JpaRepository<TableLayout, Long> {

    Optional<TableLayout> findByStoreIdAndIsActiveTrue(Long storeId);
}
