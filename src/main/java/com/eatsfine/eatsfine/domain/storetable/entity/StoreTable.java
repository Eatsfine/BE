package com.eatsfine.eatsfine.domain.storetable.entity;

import com.eatsfine.eatsfine.domain.storetable.enums.SeatsType;
import com.eatsfine.eatsfine.domain.table_layout.entity.TableLayout;
import com.eatsfine.eatsfine.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@SQLDelete(sql = "UPDATE store_table SET is_deleted = true, deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@SQLRestriction("is_deleted = false")
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

    @Column(name = "min_seat_count", nullable = false)
    private int minSeatCount;

    @Column(name = "max_seat_count", nullable = false)
    private int maxSeatCount;

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

    @Builder.Default
    @Column(name = "rating", precision = 2, scale = 1, nullable = false)
    private BigDecimal rating = BigDecimal.ZERO;

    @Column(name = "table_image_url")
    private String tableImageUrl;

    @Builder.Default
    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    // 테이블 번호 변경
    public void updateTableNumber(String tableNumber) {
        this.tableNumber = tableNumber;
    }

    // 테이블 좌석 수 변경
    public void updateSeatCount(int minSeatCount, int maxSeatCount) {
        this.minSeatCount = minSeatCount;
        this.maxSeatCount = maxSeatCount;
    }

    // 테이블 유형 변경
    public void updateSeatsType(SeatsType seatsType) {
        this.seatsType = seatsType;
    }

    // 테이블 이미지 업로드
    public void updateTableImage(String imageKey) {
        this.tableImageUrl = imageKey;
    }

    // 테이블 이미지 삭제
    public void deleteTableImage() {
        this.tableImageUrl = null;
    }
}
