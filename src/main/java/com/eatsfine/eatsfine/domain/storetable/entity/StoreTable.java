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
public class StoreTable extends BaseEntity {

    @Id
    @GeneratedValue
    private Long id;

    private String tableNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "table_layout_id", nullable = false)
    private TableLayout tableLayout; // 부모 변경

    private Integer tableSeats;

    @Enumerated(EnumType.STRING)
    @Column(name = "seats_type")
    private SeatsType seatsType;

    private int gridX;

    private int gridY;

    private int widthSpan;

    private int heightSpan;


}
