package com.eatsfine.eatsfine.domain.storetable.exception;

import com.eatsfine.eatsfine.global.apiPayload.code.BaseErrorCode;
import com.eatsfine.eatsfine.global.apiPayload.exception.GeneralException;

public class StoreTableException extends GeneralException {
    public StoreTableException(BaseErrorCode errorCode) {
        super(errorCode);
    }
}
