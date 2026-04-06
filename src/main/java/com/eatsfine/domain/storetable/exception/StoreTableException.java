package com.eatsfine.domain.storetable.exception;

import com.eatsfine.global.apipayload.code.BaseErrorCode;
import com.eatsfine.global.apipayload.exception.GeneralException;

public class StoreTableException extends GeneralException {
    public StoreTableException(BaseErrorCode errorCode) {
        super(errorCode);
    }
}
