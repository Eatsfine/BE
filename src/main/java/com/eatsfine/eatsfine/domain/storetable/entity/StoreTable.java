package com.eatsfine.eatsfine.domain.storetable.entity;

import com.eatsfine.eatsfine.domain.store.entity.Store;
import com.eatsfine.eatsfine.domain.storetable.enums.SeatsType;
import com.eatsfine.eatsfine.domain.table_layout.entity.TableLayout;
import com.eatsfine.eatsfine.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@Table(name = "store_table")
public class StoreTable extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "table_number", nullable = false, length = 30)
    private String tableNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "table_layout_id", nullable = false)
    private TableLayout tableLayout; // 부모 변경

    private Integer tableSeats; // 추후 삭제 예정

    @Enumerated(EnumType.STRING)
    @Column(name = "seats_type")
    private SeatsType seatsType;

    @Column(name = "grid_x", nullable = false)
    private int gridX;

    @Column(name = "grid_y", nullable = false)
    private int gridY;

    @Column(name = "width_span", nullable = false)
    private int widthSpan;

    @Column(name = "height_span", nullable = false)
    private int heightSpan;


}
