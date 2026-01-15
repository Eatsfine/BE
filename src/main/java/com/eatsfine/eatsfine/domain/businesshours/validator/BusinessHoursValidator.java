package com.eatsfine.eatsfine.domain.businesshours.validator;

import com.eatsfine.eatsfine.domain.businesshours.dto.BusinessHoursReqDto;
import com.eatsfine.eatsfine.domain.businesshours.exception.BusinessHoursException;
import com.eatsfine.eatsfine.domain.businesshours.status.BusinessHoursErrorStatus;

import java.time.DayOfWeek;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BusinessHoursValidator {
    public static void validate(List<BusinessHoursReqDto.Summary> dto) {

        validateComplete(dto);
        validateDuplicateDayOfWeek(dto);
        validateOpenDay(dto);
        validateClosedDay(dto);
        validateOpenCloseTime(dto);


    }

    // 7일 모두 입력 여부 검증
    private static void validateComplete(List<BusinessHoursReqDto.Summary> dto) {
        if(dto.size() != 7){
            throw new BusinessHoursException(BusinessHoursErrorStatus._BUSINESS_HOURS_NOT_COMPLETE);
        }
    }

    // open < close 검증
    private static void validateOpenCloseTime(List<BusinessHoursReqDto.Summary> dto) {
        for(BusinessHoursReqDto.Summary s: dto) {
            if(!s.isClosed()){
                if(s.openTime().isAfter(s.closeTime())) {
                    throw new BusinessHoursException(BusinessHoursErrorStatus._INVALID_BUSINESS_TIME);
                }
            }
        }
    }

    // 요일 중복 검증
    private static void validateDuplicateDayOfWeek(List<BusinessHoursReqDto.Summary> dto) {
        Set<DayOfWeek> set = new HashSet<>();
        for(BusinessHoursReqDto.Summary s: dto) {
            if(!set.add(s.dayOfWeek())) {
                throw new BusinessHoursException(BusinessHoursErrorStatus._DUPLICATE_DAY_OF_WEEK);
            }
        }
    }

    // 휴무인데 영업시간이 들어갔는지
    private static void validateClosedDay(List<BusinessHoursReqDto.Summary> dto) {
        for(BusinessHoursReqDto.Summary s: dto) {
            if(s.isClosed() && (s.openTime() != null || s.closeTime() != null)) {
                throw new BusinessHoursException(BusinessHoursErrorStatus._INVALID_CLOSED_DAY);
            }
        }
    }

    // 영업일인데 영업시간이 비버있는지
    private static void validateOpenDay(List<BusinessHoursReqDto.Summary> dto) {
        for(BusinessHoursReqDto.Summary s: dto) {
            if(!s.isClosed() && (s.openTime() == null || s.closeTime() == null)) {
                throw new BusinessHoursException(BusinessHoursErrorStatus._INVALID_OPEN_DAY);
            }
        }
    }
}
