package com.eatsfine.eatsfine.domain.region.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@Builder
@Table(name = "region")
public class Region {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 시도
    @Column(name = "province", nullable = false)
    private String province;

    // 시
    @Column(name = "city", nullable = false)
    private String city;

    // 구
    @Column(name = "district", nullable = false)
    private String district;
}
