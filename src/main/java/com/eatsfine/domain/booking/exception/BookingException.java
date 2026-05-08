package com.eatsfine.domain.booking.exception;

import com.eatsfine.global.apipayload.code.BaseErrorCode;
import com.eatsfine.global.apipayload.exception.GeneralException;

public class BookingException extends GeneralException {
    public BookingException(BaseErrorCode code) {
        super(code);
    }

    public BookingException(BaseErrorCode code, Throwable cause) {
        super(code, cause);
    }
}