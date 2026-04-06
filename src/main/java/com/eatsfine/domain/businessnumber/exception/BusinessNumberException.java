package com.eatsfine.domain.businessnumber.exception;


import com.eatsfine.global.apipayload.code.BaseErrorCode;
import com.eatsfine.global.apipayload.exception.GeneralException;

public class BusinessNumberException extends GeneralException {
    public BusinessNumberException(BaseErrorCode code) {
        super(code);
    }
}
