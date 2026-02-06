package com.eatsfine.eatsfine.domain.inquiry.repository;

import com.eatsfine.eatsfine.domain.inquiry.entity.Inquiry;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InquiryRepository extends JpaRepository<Inquiry, Long> {
}
