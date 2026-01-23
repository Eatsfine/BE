package com.eatsfine.eatsfine.domain.payment.repository;

import com.eatsfine.eatsfine.domain.payment.entity.Payment;
import com.eatsfine.eatsfine.domain.payment.enums.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByOrderId(String orderId);

    Optional<Payment> findByPaymentKey(String paymentKey);

    Page<Payment> findAllByBooking_User_Id(Long userId, Pageable pageable);

    Page<Payment> findAllByBooking_User_IdAndPaymentStatus(Long userId, PaymentStatus status, Pageable pageable);
}
