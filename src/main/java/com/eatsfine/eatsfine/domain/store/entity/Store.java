package com.eatsfine.eatsfine.domain.store.entity;

import com.eatsfine.eatsfine.domain.businesshours.entity.BusinessHours;
import com.eatsfine.eatsfine.domain.businesshours.exception.BusinessHoursException;
import com.eatsfine.eatsfine.domain.businesshours.status.BusinessHoursErrorStatus;
import com.eatsfine.eatsfine.domain.menu.entity.Menu;
import com.eatsfine.eatsfine.domain.region.entity.Region;
import com.eatsfine.eatsfine.domain.store.dto.StoreReqDto;
import com.eatsfine.eatsfine.domain.store.enums.Category;
import com.eatsfine.eatsfine.domain.store.enums.DepositRate;
import com.eatsfine.eatsfine.domain.table_layout.entity.TableLayout;
import com.eatsfine.eatsfine.domain.tableimage.entity.TableImage;
import com.eatsfine.eatsfine.domain.user.entity.User;
import com.eatsfine.eatsfine.global.apiPayload.code.status.ErrorStatus;
import com.eatsfine.eatsfine.global.apiPayload.exception.GeneralException;

import com.eatsfine.eatsfine.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

    @Column(nullable = false)
    private double latitude;

    @Column(nullable = false)
    private double longitude;

    @Column(name = "store_name", nullable = false)
    private String storeName;

    @Column(name = "business_number", nullable = false)
    private String businessNumber;

    @Column(name = "description", length = 1000, nullable = false)
    private String description;

    @Column(name = "phone_number", nullable = false)
    private String phoneNumber;

    @Column(name = "address", nullable = false)
    private String address;

    @Column(name = "main_image_key")
    private String mainImageKey;

    @Builder.Default
    @Column(name = "rating", precision = 2, scale = 1, nullable = false)
    private BigDecimal rating = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    private Category category;

    @Builder.Default
    @Column(name = "booking_interval_minutes", nullable = false)
    private int bookingIntervalMinutes = 30;

    @Enumerated(EnumType.STRING)
    @Column(name = "deposit_rate", nullable = false)
    private DepositRate depositRate;

    @Builder.Default
    @OneToMany(mappedBy = "store", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BusinessHours> businessHours = new ArrayList<>();

    @Builder.Default
    @BatchSize(size = 100)
    @OneToMany(mappedBy = "store", cascade = CascadeType.ALL)
    private List<Menu> menus = new ArrayList<>();


    @Builder.Default
    @OneToMany(mappedBy = "store", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TableImage> tableImages = new ArrayList<>();


    @Builder.Default
    @OneToMany(mappedBy = "store")
    private List<TableLayout> tableLayouts = new ArrayList<>();

    public void addBusinessHours(BusinessHours businessHours) {
        this.businessHours.add(businessHours);
        businessHours.assignStore(this);
    }

    public void removeBusinessHours(BusinessHours businessHours) {
        this.businessHours.remove(businessHours);
        businessHours.assignStore(null);
    }

    // 영업시간 변경
    public void updateBusinessHours(DayOfWeek dayOfWeek, LocalTime open, LocalTime close, boolean isClosed) {
        BusinessHours businessHours = this.businessHours.stream()
                .filter(bh -> bh.getDayOfWeek() == dayOfWeek)
                .findFirst()
                .orElseThrow(() -> new BusinessHoursException(BusinessHoursErrorStatus._BUSINESS_HOURS_DAY_NOT_FOUND));

        businessHours.update(open, close, isClosed);
    }

    // 메뉴 추가
    public void addMenu(Menu menu) {
        this.menus.add(menu);
        menu.assignStore(this);
    }

    public void addTableImage(TableImage tableImage) {
        this.tableImages.add(tableImage);
        tableImage.assignStore(this);
    }

    public void removeTableImage(TableImage tableImage) {
        this.tableImages.remove(tableImage);
        tableImage.assignStore(null);
    }

    // 가게 메인 이미지 등록
    public void updateMainImageKey(String mainImageKey) {
        this.mainImageKey = mainImageKey;
    }

    // 특정 요일의 영업시간 조회 메서드
    public BusinessHours getBusinessHoursByDay(DayOfWeek dayOfWeek) {
        return this.businessHours.stream()
                .filter(bh -> bh.getDayOfWeek() == dayOfWeek)
                .findFirst()
                .orElseThrow(() -> new GeneralException(ErrorStatus._BAD_REQUEST));
    }

    // 특정 요일의 영업시간 조회 메서드 (Optional)
    // -> 검색 로직에서는 결과들 중 하나가 영업시간이 비어있어도 나머지는 보여줘야 함
    public Optional<BusinessHours> findBusinessHoursByDay(DayOfWeek dayOfWeek) {
        return this.businessHours.stream()
                .filter(bh -> bh.getDayOfWeek() == dayOfWeek)
                .findFirst();
    }


    // 가게 기본 정보 변경 메서드
    public void updateBasicInfo(StoreReqDto.StoreUpdateDto dto) {
        if(dto.storeName() != null) {
            this.storeName = dto.storeName();
        }

        if(dto.description() != null) {
            this.description = dto.description();
        }

        if(dto.phoneNumber() != null) {
            this.phoneNumber = dto.phoneNumber();
        }

        if(dto.category() != null) {
            this.category = dto.category();
        }


        if(dto.depositRate() != null) {
            this.depositRate = dto.depositRate();
        }

        if(dto.bookingIntervalMinutes() != null) {
            this.bookingIntervalMinutes = dto.bookingIntervalMinutes();
        }
    }

}