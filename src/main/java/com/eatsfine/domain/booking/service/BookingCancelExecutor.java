package com.eatsfine.domain.booking.service;

import com.eatsfine.domain.booking.enums.BookingStatus;
import com.eatsfine.domain.booking.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class BookingCancelExecutor {

    private final BookingRepository bookingRepository;

    @Transactional(readOnly = true)
    public List<Long> findExpiredPendingIds(LocalDateTime threshold) {
        return bookingRepository.findAllByStatusAndCreatedAtBefore(BookingStatus.PENDING, threshold)
                .stream()
                .map(booking -> booking.getId())
                .toList();
    }

    // REQUIRES_NEW: 호출마다 독립 트랜잭션 — 하나 실패해도 다른 예약에 영향 없음
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void cancelIfPending(Long bookingId) {
        bookingRepository.findByIdWithLock(bookingId).ifPresent(booking -> {
            if (booking.getStatus() == BookingStatus.PENDING) {
                booking.cancel("결제 시간 초과로 인한 자동 취소");
                log.info("예약 ID {} 자동 취소 완료", bookingId);
            }
        });
    }
}
