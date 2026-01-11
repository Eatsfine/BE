package com.eatsfine.eatsfine.domain.booking.exception;

import com.eatsfine.eatsfine.global.apiPayload.code.BaseErrorCode;
import com.eatsfine.eatsfine.global.apiPayload.exception.GeneralException;

public class BookingException extends GeneralException {
    public BookingException(BaseErrorCode code) {
        super(code);
    }
}