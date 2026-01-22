package com.eatsfine.eatsfine.domain.tableblock.repository;

import com.eatsfine.eatsfine.domain.storetable.entity.StoreTable;
import com.eatsfine.eatsfine.domain.tableblock.entity.TableBlock;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface TableBlockRepository extends JpaRepository<TableBlock, Long> {
    List<TableBlock> findByStoreTableAndTargetDate(StoreTable storeTable, LocalDate targetDate);
}
