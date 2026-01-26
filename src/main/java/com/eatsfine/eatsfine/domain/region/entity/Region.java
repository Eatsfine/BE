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

    // 시/도 (예: 서울특별시, 경기도, 세종특별자치시)
    @Column(name = "sido", nullable = false)
    private String sido;

    // 시/군/구 (예: 강남구, 성남시 분당구, "")
    @Column(name = "sigungu", nullable = false)
    private String sigungu;

    // 법정동 (예: 역삼동, 서현동, 어진동)
    @Column(name = "bname", nullable = false)
    private String bname;
}
