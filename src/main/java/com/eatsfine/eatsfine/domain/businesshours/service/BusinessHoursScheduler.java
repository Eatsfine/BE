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

    @Scheduled(cron = "0 0 0 * * *") // 매일 자정
    @Transactional
    public void applyPendingBreakTimes() {
        log.info("[Scheduler] 브레이크 타임 지연 반영 작업 시작");

        List<BusinessHours> targets = businessHoursRepository.findAllByEffectiveDate(LocalDate.now());

        if(targets.isEmpty()) {
            log.info("[Scheduler] 오늘 반영할 항목이 없습니다.");
            return;
        }

        targets.forEach(BusinessHours::applyPendingBreakTime);
        log.info("[Scheduler] 총 {}건의 브레이크 타임이 성공적으로 갱신되었습니다.", targets.size());
    }
}
