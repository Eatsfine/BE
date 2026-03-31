package com.eatsfine.domain.businesshours.exception;

import com.eatsfine.global.apipayload.code.BaseErrorCode;
import com.eatsfine.global.apipayload.exception.GeneralException;

public class BusinessHoursException extends GeneralException {
    public BusinessHoursException(BaseErrorCode code){
        super(code);
    }
}
