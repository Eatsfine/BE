package com.eatsfine.eatsfine.domain.store.repository;

import com.eatsfine.eatsfine.domain.store.dto.projection.StoreSearchResult;
import com.eatsfine.eatsfine.domain.store.entity.Store;
import com.eatsfine.eatsfine.domain.store.enums.Category;
import com.eatsfine.eatsfine.domain.store.enums.StoreSortType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StoreRepository extends JpaRepository<Store, Long> {
    // JPQL에서는 SELECT alias(distance)를 WHERE/HAVING 절에서 재사용할 수 없어
    // 동일한 거리 계산식을 WHERE, ORDER BY 절에 중복 작성함
    // → 추후 QueryDSL 도입 시 계산식 분리 및 가독성 개선 예정
    @Query("""
    SELECT new com.eatsfine.eatsfine.domain.store.dto.projection.StoreSearchResult(
        s,
        CAST((6371 * acos(
            cos(radians(:lat)) * cos(radians(s.latitude))
          * cos(radians(s.longitude) - radians(:lng))
          + sin(radians(:lat)) * sin(radians(s.latitude))
        )) AS double)
    )
    FROM Store s
    WHERE (:category IS NULL OR s.category = :category)
    AND (
        6371 * acos(
            cos(radians(:lat)) * cos(radians(s.latitude))
          * cos(radians(s.longitude) - radians(:lng))
          + sin(radians(:lat)) * sin(radians(s.latitude))
        )
    ) <= :radius
    ORDER BY
      CASE WHEN :sort = 'DISTANCE' THEN
        (6371 * acos(
            cos(radians(:lat)) * cos(radians(s.latitude))
          * cos(radians(s.longitude) - radians(:lng))
          + sin(radians(:lat)) * sin(radians(s.latitude))
        ))
      END ASC,
      CASE WHEN :sort = 'RATING' THEN s.rating END DESC
    """)
    Page<StoreSearchResult> searchStores(
            @Param("lat") Double lat,
            @Param("lng") Double lng,
            @Param("radius") Double radius,
            @Param("category") Category category,
            @Param("sort") StoreSortType sort,
            Pageable pageable
    );
}