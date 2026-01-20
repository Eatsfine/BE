package com.eatsfine.eatsfine.domain.table_layout.exception;

import com.eatsfine.eatsfine.global.apiPayload.code.BaseErrorCode;
import com.eatsfine.eatsfine.global.apiPayload.exception.GeneralException;

public class TableLayoutException extends GeneralException {
    public TableLayoutException(BaseErrorCode code) {
        super(code);
    }
}
