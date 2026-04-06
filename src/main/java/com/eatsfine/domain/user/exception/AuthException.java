package com.eatsfine.domain.user.exception;


import com.eatsfine.global.apipayload.code.BaseErrorCode;
import com.eatsfine.global.apipayload.exception.GeneralException;

public class AuthException extends GeneralException {
    public AuthException(BaseErrorCode code) {
        super(code);
    }
}