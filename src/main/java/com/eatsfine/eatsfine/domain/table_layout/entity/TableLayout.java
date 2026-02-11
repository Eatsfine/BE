package com.eatsfine.eatsfine.domain.table_layout.entity;

import com.eatsfine.eatsfine.domain.store.entity.Store;
import com.eatsfine.eatsfine.domain.storetable.entity.StoreTable;
import com.eatsfine.eatsfine.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE table_layout SET is_deleted = true, is_active = false, deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@SQLRestriction("is_deleted = false")
@Table(
        name = "table_layout",
        uniqueConstraints = {@UniqueConstraint(name = "uk_table_layout_store_active", columnNames = {"store_active_key"})}
)
public class TableLayout extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @Column(name = "grid_rows", nullable = false)
    private int lows;

    @Column(name = "grid_cols", nullable = false)
    private int cols;

    @Column(name = "is_active", nullable = false)
    private boolean isActive;

    // 가상 컬럼 추가 (JPA에서는 직접 사용하지 않지만 DB 레벨에서 동작)
    // is_active = true일 때만 store_id 값을 가지고, false일 때는 NULL
    @Column(
            name = "store_active_key",
            columnDefinition = "VARCHAR(50) GENERATED ALWAYS AS (CASE WHEN is_active = true THEN CONCAT('store_', store_id) ELSE NULL END) STORED",
            insertable = false,
            updatable = false
    )
    private String storeActiveKey;

    @Column(name = "is_deleted", nullable = false)
    @Builder.Default
    private boolean isDeleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Builder.Default
    @OneToMany(mappedBy = "tableLayout", cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE})
    private List<StoreTable> tables = new ArrayList<>();

}
