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

    // 시/도 (예: 서울특별시, 경기도)
    @Column(name = "province", nullable = false)
    private String province;

    // 시/군/구 (예: 강남구, 성남시, 가평군)
    @Column(name = "city", nullable = false)
    private String city;

    // 구/읍/면/동 (예: 분당구, 진접읍, 역삼동 ..)
    @Column(name = "district", nullable = false)
    private String district;
}
