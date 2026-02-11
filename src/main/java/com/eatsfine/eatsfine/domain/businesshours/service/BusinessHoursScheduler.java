package com.eatsfine.eatsfine.domain.businesshours.service;

import com.eatsfine.eatsfine.domain.businesshours.entity.BusinessHours;
import com.eatsfine.eatsfine.domain.businesshours.repository.BusinessHoursRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class BusinessHoursScheduler {

    private final BusinessHoursRepository businessHoursRepository;

    @Scheduled(cron = "0 0 0 * * *")
    public void applyPendingBreakTimes() {
        log.info("[Scheduler] 브레이크 타임 지연 반영 작업 시작");

        List<BusinessHours> pendingList = businessHoursRepository.findAllByEffectiveDateLessThanEqualAndEffectiveDateIsNotNull(LocalDate.now());

        // 전체 대상 건수 로그
        log.info("[Scheduler] 처리 대상 건수: {}건", pendingList.size());

        int successCount = 0;

        for (BusinessHours bh : pendingList) {
            try {
                processEachPendingTime(bh);
                successCount++;

            } catch (Exception e) {
                // 개별 건 처리 중 에러 발생 시 로그 남기고 다음 건 진행
                log.error("[Scheduler Exception] 반영 실패 - BH ID: {}, Error: {}", bh.getId(), e.getMessage());
            }
        }
        log.info("[Scheduler] 반영 작업 완료. (성공: {}/{} 건)", successCount, pendingList.size());
    }

    @Transactional
    public void processEachPendingTime(BusinessHours bh) {
        if((bh.getNewBreakStartTime() == null) ^ (bh.getNewBreakEndTime() == null)) {
            log.warn("[XOR Error] ID: {}", bh.getId());
            bh.clearPendingBreakTime();
            return;
        }
        bh.applyPendingBreakTime();
    }
}
