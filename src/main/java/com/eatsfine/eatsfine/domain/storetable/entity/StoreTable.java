package com.eatsfine.eatsfine.domain.storetable.entity;

import com.eatsfine.eatsfine.domain.store.entity.Store;
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
    @JoinColumn(name = "store")
    private Store store;

    private Integer tableSeats;


}
