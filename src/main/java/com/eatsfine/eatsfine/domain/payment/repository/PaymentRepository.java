package com.eatsfine.eatsfine.domain.payment.repository;

import com.eatsfine.eatsfine.domain.payment.entity.Payment;
import com.eatsfine.eatsfine.domain.payment.enums.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
        Optional<Payment> findByOrderId(String orderId);

        Optional<Payment> findByPaymentKey(String paymentKey);

        @Query(value = "SELECT p FROM Payment p JOIN FETCH p.booking b JOIN FETCH b.store WHERE b.user.id = :userId", countQuery = "SELECT COUNT(p) FROM Payment p JOIN p.booking b WHERE b.user.id = :userId")
        Page<Payment> findAllByUserIdWithDetails(@Param("userId") Long userId, Pageable pageable);

        @Query(value = "SELECT p FROM Payment p JOIN FETCH p.booking b JOIN FETCH b.store WHERE b.user.id = :userId AND p.paymentStatus = :status", countQuery = "SELECT COUNT(p) FROM Payment p JOIN p.booking b WHERE b.user.id = :userId AND p.paymentStatus = :status")
        Page<Payment> findAllByUserIdAndStatusWithDetails(@Param("userId") Long userId,
                        @Param("status") PaymentStatus status, Pageable pageable);

        @Query(value = "SELECT p FROM Payment p JOIN FETCH p.booking b JOIN FETCH b.store s WHERE s.owner.id = :userId", countQuery = "SELECT COUNT(p) FROM Payment p JOIN p.booking b JOIN b.store s WHERE s.owner.id = :userId")
        Page<Payment> findAllByOwnerIdWithDetails(@Param("userId") Long userId, Pageable pageable);

        @Query(value = "SELECT p FROM Payment p JOIN FETCH p.booking b JOIN FETCH b.store s WHERE s.owner.id = :userId AND p.paymentStatus = :status", countQuery = "SELECT COUNT(p) FROM Payment p JOIN p.booking b JOIN b.store s WHERE s.owner.id = :userId AND p.paymentStatus = :status")
        Page<Payment> findAllByOwnerIdAndStatusWithDetails(@Param("userId") Long userId,
                        @Param("status") PaymentStatus status, Pageable pageable);

        @Query("SELECT p FROM Payment p JOIN FETCH p.booking b JOIN FETCH b.store JOIN FETCH b.user WHERE p.id = :paymentId")
        Optional<Payment> findByIdWithDetails(@Param("paymentId") Long paymentId);
}
