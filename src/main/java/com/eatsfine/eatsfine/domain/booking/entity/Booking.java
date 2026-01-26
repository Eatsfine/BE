package com.eatsfine.eatsfine.domain.booking.entity;

import com.eatsfine.eatsfine.domain.booking.entity.mapping.BookingTable;
import com.eatsfine.eatsfine.domain.booking.enums.BookingStatus;
import com.eatsfine.eatsfine.domain.payment.entity.Payment;
import com.eatsfine.eatsfine.domain.store.entity.Store;
import com.eatsfine.eatsfine.domain.storetable.entity.StoreTable;
import com.eatsfine.eatsfine.domain.user.entity.User;
import com.eatsfine.eatsfine.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Getter
@Table(name = "booking")
public class Booking extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @Builder.Default
    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BookingTable> bookingTables = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL) // 결제 내역은 중요하므로 orphanRemoval은 신중하게, 우선 제외
    private List<Payment> payments = new ArrayList<>();

    @Column(name = "party_size", nullable = false)
    private Integer partySize;

    // 테이블 분리 허용 여부
    @Builder.Default
    @Column(name = "is_split_accepted", nullable = false)
    private boolean isSplitAccepted = false;

    // 예약 날짜 (YYYY-MM-DD)
    @Column(name = "booking_date", nullable = false)
    private LocalDate bookingDate;

    // 예약 시간 (HH:mm)
    @Column(name = "booking_time", nullable = false)
    private LocalTime bookingTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private BookingStatus status;

    public void addBookingTable(StoreTable storeTable) {
        BookingTable bookingTable = BookingTable.builder()
                .booking(this)
                .storeTable(storeTable)
                .build();
        this.bookingTables.add(bookingTable);
    }

    private Integer depositAmount;

    private String cancelReason;

    public void confirm() {
        this.status = BookingStatus.CONFIRMED;
    }

    public void cancel(String cancelReason)
    {
        this.status = BookingStatus.CANCELED;
        this.cancelReason = cancelReason;
    }

}
