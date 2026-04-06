package com.eatsfine.domain.user.exception;

import com.eatsfine.global.apipayload.code.BaseErrorCode;
import com.eatsfine.global.apipayload.exception.GeneralException;

public class UserException extends GeneralException {
    public UserException(BaseErrorCode code) {
        super(code);
    }
}
