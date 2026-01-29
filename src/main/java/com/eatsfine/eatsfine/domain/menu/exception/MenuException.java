package com.eatsfine.eatsfine.domain.menu.exception;

import com.eatsfine.eatsfine.global.apiPayload.code.BaseErrorCode;
import com.eatsfine.eatsfine.global.apiPayload.exception.GeneralException;

public class MenuException extends GeneralException {
    public MenuException(BaseErrorCode code) {
        super(code);
    }
}
