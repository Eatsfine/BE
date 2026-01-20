package com.eatsfine.eatsfine.domain.payment.exception;

import com.eatsfine.eatsfine.global.apiPayload.code.BaseErrorCode;
import com.eatsfine.eatsfine.global.apiPayload.exception.GeneralException;

public class PaymentException extends GeneralException {

    public PaymentException(BaseErrorCode code) {
        super(code);
    }
}
