package com.eatsfine.domain.booking.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class BookingScheduler {

    private final BookingCancelExecutor bookingCancelExecutor;

    /**
     * 결제 미완료(PENDING) 상태로 10분이 경과한 예약을 주기적으로 취소 처리
     * cron: 0분부터 10분 단위로 실행 (0, 10, 20, 30, 40, 50분)
     *
     * 각 예약을 독립 트랜잭션(REQUIRES_NEW)으로 처리하여
     * 낙관적 락 충돌 등 일부 실패가 전체 배치에 영향을 주지 않음
     */
    @Scheduled(cron = "0 0/10 * * * *")
    public void cleanupExpiredPendingBookings() {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(10);

        List<Long> expiredIds = bookingCancelExecutor.findExpiredPendingIds(threshold);

        if (expiredIds.isEmpty()) {
            return;
        }

        log.info("스케줄러 실행: 만료된 PENDING 예약 {}건 처리 시작", expiredIds.size());

        int canceledCount = 0;
        int skippedCount = 0;
        for (Long id : expiredIds) {
            try {
                if (bookingCancelExecutor.cancelIfPending(id)) canceledCount++;
                else skippedCount++;
            } catch (Exception e) {
                log.warn("예약 ID {} 자동 취소 실패 — 다음 실행에서 재시도: {}", id, e.getMessage());
            }
        }

        log.info("스케줄러 완료: 취소 {}건 / 스킵 {}건 / 시도 {}건",
                canceledCount, skippedCount, expiredIds.size());
    }
}
