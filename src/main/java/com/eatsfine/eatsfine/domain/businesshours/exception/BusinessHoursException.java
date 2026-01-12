package com.eatsfine.eatsfine.domain.businesshours.exception;

import com.eatsfine.eatsfine.global.apiPayload.code.BaseErrorCode;
import com.eatsfine.eatsfine.global.apiPayload.exception.GeneralException;

public class BusinessHoursException extends GeneralException {
    public BusinessHoursException(BaseErrorCode code){
        super(code);
    }
}
