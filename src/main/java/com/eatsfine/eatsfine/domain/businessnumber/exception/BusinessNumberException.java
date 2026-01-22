package com.eatsfine.eatsfine.domain.businessnumber.exception;


import com.eatsfine.eatsfine.global.apiPayload.code.BaseErrorCode;
import com.eatsfine.eatsfine.global.apiPayload.exception.GeneralException;

public class BusinessNumberException extends GeneralException {
    public BusinessNumberException(BaseErrorCode code) {
        super(code);
    }
}
