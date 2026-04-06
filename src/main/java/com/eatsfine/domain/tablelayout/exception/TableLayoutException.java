package com.eatsfine.domain.tablelayout.exception;

import com.eatsfine.global.apipayload.code.BaseErrorCode;
import com.eatsfine.global.apipayload.exception.GeneralException;

public class TableLayoutException extends GeneralException {
    public TableLayoutException(BaseErrorCode code) {
        super(code);
    }
}
