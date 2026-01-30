package com.eatsfine.eatsfine.domain.payment.entity;

import com.eatsfine.eatsfine.domain.booking.entity.Booking;
import com.eatsfine.eatsfine.domain.payment.enums.PaymentMethod;
import com.eatsfine.eatsfine.domain.payment.enums.PaymentProvider;
import com.eatsfine.eatsfine.domain.payment.enums.PaymentStatus;
import com.eatsfine.eatsfine.domain.payment.enums.PaymentType;
import com.eatsfine.eatsfine.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "payment")
public class Payment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @Column(name = "order_id", nullable = false)
    private String orderId;

    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    @Column(name = "payment_key")
    private String paymentKey;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_provider")
    private PaymentProvider paymentProvider;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method")
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false)
    private PaymentStatus paymentStatus;

    @Column(name = "requested_at", nullable = false)
    private LocalDateTime requestedAt;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_type", nullable = false)
    private PaymentType paymentType;

    @Column(name = "receipt_url")
    private String receiptUrl;

    public void completePayment(LocalDateTime approvedAt, PaymentMethod method, String paymentKey,
            PaymentProvider provider, String receiptUrl) {
        this.paymentStatus = PaymentStatus.COMPLETED;
        this.approvedAt = approvedAt;
        this.paymentMethod = method;
        this.paymentKey = paymentKey;
        this.paymentProvider = provider;
        this.receiptUrl = receiptUrl;
    }

    public void failPayment() {
        this.paymentStatus = PaymentStatus.FAILED;
    }

    public void cancelPayment() {
        this.paymentStatus = PaymentStatus.REFUNDED;
    }
}
