package com.eatsfine.eatsfine.domain.tableblock.exception;

import com.eatsfine.eatsfine.global.apiPayload.code.BaseErrorCode;
import com.eatsfine.eatsfine.global.apiPayload.exception.GeneralException;

public class TableBlockException extends GeneralException {
    public TableBlockException(BaseErrorCode errorCode) {
        super(errorCode);
    }
}
