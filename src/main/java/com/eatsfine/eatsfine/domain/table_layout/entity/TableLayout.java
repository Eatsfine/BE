package com.eatsfine.eatsfine.domain.table_layout.entity;

import com.eatsfine.eatsfine.domain.store.entity.Store;
import com.eatsfine.eatsfine.domain.storetable.entity.StoreTable;
import com.eatsfine.eatsfine.global.entity.BaseEntity;
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
@SQLDelete(sql = "UPDATE table_layout SET is_deleted = true WHERE id = ?")
@SQLRestriction("is_deleted = false")
@Table(name = "table_layout")
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

    @Column(name = "is_deleted", nullable = false)
    @Builder.Default
    private boolean isDeleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @OneToMany(mappedBy = "tableLayout", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<StoreTable> tables = new ArrayList<>();

}
