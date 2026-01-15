package com.eatsfine.eatsfine.domain.store.repository;

import com.eatsfine.eatsfine.domain.region.entity.QRegion;
import com.eatsfine.eatsfine.domain.store.dto.projection.StoreSearchResult;
import com.eatsfine.eatsfine.domain.store.entity.QStore;
import com.eatsfine.eatsfine.domain.store.enums.Category;
import com.eatsfine.eatsfine.domain.store.enums.StoreSortType;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
@RequiredArgsConstructor
public class StoreRepositoryImpl implements StoreRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<StoreSearchResult> searchStores(
            Double lat,
            Double lng,
            String keyword,
            Category category,
            StoreSortType sort,
            String province,
            String city,
            String district,
            Pageable pageable
    ) {
        QStore store = QStore.store;
        QRegion region = QRegion.region;

        // 거리 계산
        NumberExpression<Double> distanceExpression = calculateDistance(lat, lng, store);

        // 동적 조건 생성
        BooleanBuilder whereClause = new BooleanBuilder();

        // 키워드 필터
        if (keyword != null && !keyword.isBlank()) {
            whereClause.and(keywordContains(store, keyword));
        }

        // 카테고리 필터
        if (category != null) {
            whereClause.and(store.category.eq(category));
        }

        // 시/도 필터
        if (province != null) {
            whereClause.and(region.province.eq(province));
        }

        // 시도 필터
        if (city != null) {
            whereClause.and(region.city.eq(city));
        }

        // 구 필터
        if (district != null) {
            whereClause.and(region.district.eq(district));
        }

        // 정렬 조건 생성
        OrderSpecifier<?> orderSpecifier = createOrderSpecifier(sort, store, distanceExpression);

        // 메인 쿼리 실행
        List<StoreSearchResult> results = queryFactory
                .select(Projections.constructor(
                        StoreSearchResult.class,
                        store,
                        distanceExpression
                ))
                .from(store)
                .join(store.region, region)
                .where(whereClause)
                .orderBy(orderSpecifier)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // 전체 개수 조회
        Long total = queryFactory
                .select(store.count())
                .from(store)
                .join(store.region, region)
                .where(whereClause)
                .fetchOne();

        return new PageImpl<>(results, pageable, total != null ? total : 0);
    }

    // 두 지점 간 거리 계산 (km)
    private NumberExpression<Double> calculateDistance(Double lat, Double lng, QStore store) {
       // 위도/경도를 라디안으로 변환
        NumberExpression<Double> latRad = Expressions.numberTemplate(
                Double.class, "radians({0})", lat
        );
        NumberExpression<Double> lngRad = Expressions.numberTemplate(
                Double.class, "radians({0})", lng
        );
        NumberExpression<Double> storeLatRad = Expressions.numberTemplate(
                Double.class, "radians({0})", store.latitude
        );
        NumberExpression<Double> storeLngRad = Expressions.numberTemplate(
                Double.class, "radians({0})", store.longitude
        );

        return Expressions.numberTemplate(
                Double.class,
                "6371 * acos(cos({0}) * cos({1}) * cos({2} - {3}) + sin({0}) * sin({1}))",
                latRad, storeLatRad, storeLngRad, lngRad
        );

    }

    // 정렬 조건 생성
    private OrderSpecifier<?> createOrderSpecifier(
            StoreSortType sort,
            QStore store,
            NumberExpression<Double> distanceExpression
    ) {
        return switch (sort) {
            case DISTANCE -> distanceExpression.asc();
            case RATING -> store.rating.desc();
        };
    }

    // 키워드 찾기 메서드
    private BooleanExpression keywordContains(QStore store, String keyword) {
        return store.storeName.containsIgnoreCase(keyword)
                .or(store.description.containsIgnoreCase(keyword)
                        .or(store.address.containsIgnoreCase(keyword)));
    }
}
