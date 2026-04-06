package com.eatsfine.domain.menu.exception;

import com.eatsfine.global.apipayload.code.BaseErrorCode;
import com.eatsfine.global.apipayload.exception.GeneralException;

public class MenuException extends GeneralException {
    public MenuException(BaseErrorCode code) {
        super(code);
    }
}
