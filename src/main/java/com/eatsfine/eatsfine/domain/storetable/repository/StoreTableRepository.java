package com.eatsfine.eatsfine.domain.storetable.repository;

import com.eatsfine.eatsfine.domain.storetable.entity.StoreTable;
import com.eatsfine.eatsfine.domain.table_layout.entity.TableLayout;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StoreTableRepository extends JpaRepository<StoreTable,Long> {

    // 비관적 쓰기 락을 걸어 조회
    // 다른 트랜잭션이 이 테이블들을 수정하거나 동시에 락을 거는 것을 방지
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT st FROM StoreTable st WHERE st.id IN :ids")
    List<StoreTable> findAllByIdWithLock(@Param("ids") List<Long> ids);

    // 특정 레이아웃에서 특정 번호를 가진 활성 테이블 조회, 테이블 번호 중복 체크용
    Optional<StoreTable> findByTableLayoutAndTableNumberAndIsDeletedFalse(TableLayout tableLayout, String tableNumber);
}
