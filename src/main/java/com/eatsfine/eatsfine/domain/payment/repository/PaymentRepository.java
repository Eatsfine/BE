package com.eatsfine.eatsfine.domain.payment.repository;

import com.eatsfine.eatsfine.domain.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
}
