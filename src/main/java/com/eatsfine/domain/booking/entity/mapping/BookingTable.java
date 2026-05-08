package com.eatsfine.domain.booking.entity.mapping;

import com.eatsfine.domain.booking.entity.Booking;
import com.eatsfine.domain.storetable.entity.StoreTable;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Getter
@Table(
    name = "booking_table",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_booking_table_slot",
        columnNames = {"store_table_id", "booking_date", "booking_time", "is_active"}
    )
)
public class BookingTable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_table_id")
    private StoreTable storeTable;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id")
    private Booking booking;

    // 유니크 제약 적용을 위해 Booking의 날짜/시간을 비정규화하여 저장
    @Column(name = "booking_date", nullable = false)
    private LocalDate bookingDate;

    @Column(name = "booking_time", nullable = false)
    private LocalTime bookingTime;

    // true = 활성 예약 슬롯 (유니크 제약 적용)
    // null = 취소된 슬롯 — MySQL은 NULL을 유니크 인덱스에서 중복으로 보지 않으므로 동일 시간대 재예약 허용
    @Column(name = "is_active")
    private Boolean isActive = true;

    public void deactivate() {
        this.isActive = null;
    }
}
