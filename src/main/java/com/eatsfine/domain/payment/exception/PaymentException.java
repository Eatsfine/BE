package com.eatsfine.domain.payment.exception;

import com.eatsfine.global.apipayload.code.BaseErrorCode;
import com.eatsfine.global.apipayload.exception.GeneralException;

public class PaymentException extends GeneralException {

    public PaymentException(BaseErrorCode code) {
        super(code);
    }
}
