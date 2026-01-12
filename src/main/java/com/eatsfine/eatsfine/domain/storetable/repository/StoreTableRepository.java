package com.eatsfine.eatsfine.domain.storetable.repository;

import com.eatsfine.eatsfine.domain.storetable.entity.StoreTable;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface StoreTableRepository extends JpaRepository<StoreTable,Long> {

    // 비관적 쓰기 락을 걸어 조회
    // 다른 트랜잭션이 이 테이블들을 수정하거나 동시에 락을 거는 것을 방지
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT st FROM StoreTable st WHERE st.id IN :ids")
    List<StoreTable> findAllByIdWithLock(@Param("ids") List<Long> ids);
}
