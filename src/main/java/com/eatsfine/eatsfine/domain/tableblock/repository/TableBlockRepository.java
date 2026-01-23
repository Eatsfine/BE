package com.eatsfine.eatsfine.domain.tableblock.repository;

import com.eatsfine.eatsfine.domain.storetable.entity.StoreTable;
import com.eatsfine.eatsfine.domain.tableblock.entity.TableBlock;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface TableBlockRepository extends JpaRepository<TableBlock, Long> {
    List<TableBlock> findByStoreTableAndTargetDate(StoreTable storeTable, LocalDate targetDate);

    Optional<TableBlock> findByStoreTableAndTargetDateAndStartTime(StoreTable storeTable, LocalDate targetDate, LocalTime startTime);
}
