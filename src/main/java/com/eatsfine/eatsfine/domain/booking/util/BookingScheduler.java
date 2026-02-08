package com.eatsfine.eatsfine.domain.booking.util;

import com.eatsfine.eatsfine.domain.booking.entity.Booking;
import com.eatsfine.eatsfine.domain.booking.enums.BookingStatus;
import com.eatsfine.eatsfine.domain.booking.repository.BookingRepository;
import jakarta.transaction.Transactional;
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

    private final BookingRepository bookingRepository;

    /**
     * 결제 미완료(PENDING) 상태로 10분이 경과한 예약을 주기적으로 취소 처리
     * cron: 0분부터 10분 단위로 실행 (0, 10, 20, 30, 40, 50분)
     */
    @Scheduled(cron = "0 0/10 * * * *")
    @Transactional
    public void cleanupExpiredPendingBookings() {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(10);

        // 1. 10분 전보다 이전에 생성되었고, 여전히 PENDING인 예약 조회
        List<Booking> expiredBookings = bookingRepository.findAllByStatusAndCreatedAtBefore(
                BookingStatus.PENDING,
                threshold
        );

        if (expiredBookings.isEmpty()) {
            return;
        }

        log.info("스케줄러 실행: 만료된 PENDING 예약 {}건을 취소 처리합니다.", expiredBookings.size());

        // 2. 상태 변경 및 로그 기록
        expiredBookings.forEach(booking -> {
            booking.cancel("결제 시간 초과로 인한 자동 취소");
        });

    }
}
