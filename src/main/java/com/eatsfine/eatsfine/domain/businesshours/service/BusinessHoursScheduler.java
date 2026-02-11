package com.eatsfine.eatsfine.domain.businesshours.service;

import com.eatsfine.eatsfine.domain.businesshours.entity.BusinessHours;
import com.eatsfine.eatsfine.domain.businesshours.exception.BusinessHoursException;
import com.eatsfine.eatsfine.domain.businesshours.repository.BusinessHoursRepository;
import com.eatsfine.eatsfine.domain.businesshours.status.BusinessHoursErrorStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class BusinessHoursScheduler {

    private final BusinessHoursRepository businessHoursRepository;
    private final TransactionTemplate transactionTemplate;

    @Scheduled(cron = "0 0 0 * * *")
    public void applyPendingBreakTimes() {
        log.info("[Scheduler] 브레이크 타임 지연 반영 작업 시작");

        List<BusinessHours> pendingList = businessHoursRepository.findAllByEffectiveDateLessThanEqualAndEffectiveDateIsNotNull(LocalDate.now());

        int successCount = 0;
        int failCount = 0;
        int warnCount = 0; // 데이터 불일치(XOR) 건수

        // 전체 대상 건수 로그
        log.info("[Scheduler] 처리 대상 건수: {}건", pendingList.size());

        for (BusinessHours bh : pendingList) {
            try {
                Boolean isApplied = transactionTemplate.execute(status -> processEachPendingTime(bh.getId()));
                if(Boolean.TRUE.equals(isApplied)) {
                    successCount++;
                } else {
                    warnCount++; // XOR 등으로 인해 초기화만 된 경우
                }
            } catch (Exception e) {
                failCount++;
                // 개별 건 처리 중 에러 발생 시 로그 남기고 다음 건 진행
                log.error("[Scheduler Exception] 반영 실패 - BH ID: {}", bh.getId(), e);
            }
        }
        log.info("[Scheduler] 반영 작업 완료. (성공: {}/{} 건)", successCount, pendingList.size());
    }

    public boolean processEachPendingTime(Long bhId) {

        BusinessHours bh = businessHoursRepository.findById(bhId)
                .orElseThrow(() -> new BusinessHoursException(BusinessHoursErrorStatus._BUSINESS_HOURS_NOT_FOUND));

        if((bh.getNewBreakStartTime() == null) ^ (bh.getNewBreakEndTime() == null)) {
            log.warn("[XOR Error] ID: {}", bh.getId());
            bh.clearPendingBreakTime();
            return false; // 초기화만 한 경우 false 리턴
        }
        bh.applyPendingBreakTime();
        return true; // 정상 반영한 경우 true 리턴
    }
}
