package com.eatsfine.eatsfine.domain.store.entity;

import com.eatsfine.eatsfine.domain.businesshours.entity.BusinessHours;
import com.eatsfine.eatsfine.domain.region.entity.Region;
import com.eatsfine.eatsfine.domain.store.enums.Category;
import com.eatsfine.eatsfine.domain.store.enums.StoreApprovalStatus;
import com.eatsfine.eatsfine.domain.storetable.entity.StoreTable;
import com.eatsfine.eatsfine.domain.tableimage.entity.TableImage;
import com.eatsfine.eatsfine.domain.user.entity.User;
import com.eatsfine.eatsfine.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Table(name = "store")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@Builder
@Entity
public class Store extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_id", nullable = false)
    private Region region;

    @Column(name = "store_name", nullable = false)
    private String storeName;

    @Lob
    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "phone_number", nullable = false)
    private String phoneNumber;

    @Column(name = "address", nullable = false)
    private String address;

    @Column(name = "main_image_url", nullable = false)
    private String mainImageUrl;

    @Builder.Default
    @Column(name = "rating", precision = 2, scale = 1, nullable = false)
    private BigDecimal rating = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    private Category category;

    @Column(name = "min_price", nullable = false)
    private int minPrice;

    @Column(name = "max_price", nullable = false)
    private int maxPrice;

    @Enumerated(EnumType.STRING)
    @Column(name = "store_approval_status", nullable = false)
    private StoreApprovalStatus approvalStatus;

    @Builder.Default
    @Column(name = "booking_interval_minutes", nullable = false)
    private int bookingIntervalMinutes = 30;

    @Builder.Default
    @OneToMany(mappedBy = "store", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BusinessHours> businessHours = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "store", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TableImage> tableImages = new ArrayList<>();

    // 추후 StoreTable 엔티티 개발 완료 시 추가 예정
    //@OneToMany(mappedBy = "store")
    //private List<StoreTable> storeTables = new ArrayList<>();

    public void addBusinessHours(BusinessHours businessHours) {
        this.businessHours.add(businessHours);
        businessHours.assignStore(this);
    }

    public void removeBusinessHours(BusinessHours businessHours) {
        this.businessHours.remove(businessHours);
        businessHours.assignStore(this);
    }

    public void addTableImage(TableImage tableImage) {
        this.tableImages.add(tableImage);
        tableImage.assignStore(this);
    }

    public void removeTableImage(TableImage tableImage) {
        this.tableImages.remove(tableImage);
        tableImage.assignStore(null);
    }

    // StoreTable에 대한 연관관계 편의 메서드는 추후 추가 예정

}
