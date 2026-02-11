package com.eatsfine.eatsfine.domain.businesshours.entity;

import com.eatsfine.eatsfine.domain.store.entity.Store;
import com.eatsfine.eatsfine.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Getter
@Table(name = "business_hours")
public class BusinessHours extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false)
    private DayOfWeek dayOfWeek;

    @Column(name = "open_time")
    private LocalTime openTime;

    @Column(name = "close_time")
    private LocalTime closeTime;

    @Column(name = "break_start_time")
    private LocalTime breakStartTime;

    @Column(name = "break_end_time")
    private LocalTime breakEndTime;

    @Column(name = "new_break_start_time")
    private LocalTime newBreakStartTime;

    @Column(name = "new_break_end_time")
    private LocalTime newBreakEndTime;

    @Column(name = "effective_date")
    private LocalDate effectiveDate;

    // 휴일 여부 (특정 요일 고정 휴무)
    @Builder.Default
    @Column(name = "is_closed", nullable = false)
    private boolean isClosed = false;

    public void assignStore(Store store) {
        this.store = store;
    }

    // 영업시간 변경
    public void update(LocalTime open, LocalTime close, boolean isClosed) {
        this.openTime = open;
        this.closeTime = close;
        this.isClosed = isClosed;
    }

    // 브레이크타임 변경
    public void updateBreakTime(LocalTime startTime, LocalTime endTime, LocalDate effectiveDate) {
        // 오늘(혹은 과거) 날짜라면 -> 즉시 반영
        if (effectiveDate == null || !effectiveDate.isAfter(LocalDate.now())) {
            this.breakStartTime = startTime;
            this.breakEndTime = endTime;
            // 대기 중인 데이터 초기화
            this.newBreakStartTime = null;
            this.newBreakEndTime = null;
            this.effectiveDate = null;
        }
        // 미래 날짜라면 -> 대기열에 저장
        else {
            this.newBreakStartTime = startTime;
            this.newBreakEndTime = endTime;
            this.effectiveDate = effectiveDate;
        }
    }

    // 대기열 -> 실제 반영 (스케줄러 호출)
    public void applyPendingBreakTime() {
        if (this.newBreakStartTime != null && this.newBreakEndTime != null) {
            this.breakStartTime = newBreakStartTime;
            this.breakEndTime = newBreakEndTime;

            // 반영 후 대기열 비우기
            this.newBreakStartTime = null;
            this.newBreakEndTime = null;
            this.effectiveDate = null;
        }
    }

    public void clearPendingBreakTime() {
        this.newBreakStartTime = null;
        this.newBreakEndTime = null;
        this.effectiveDate = null;
    }
}
