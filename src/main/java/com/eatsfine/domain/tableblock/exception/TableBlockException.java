package com.eatsfine.domain.tableblock.exception;

import com.eatsfine.global.apipayload.code.BaseErrorCode;
import com.eatsfine.global.apipayload.exception.GeneralException;

public class TableBlockException extends GeneralException {
    public TableBlockException(BaseErrorCode errorCode) {
        super(errorCode);
    }
}
