package com.eatsfine.eatsfine.domain.region.exception;

import com.eatsfine.eatsfine.global.apiPayload.code.BaseErrorCode;
import com.eatsfine.eatsfine.global.apiPayload.exception.GeneralException;

public class RegionException extends GeneralException {
    public RegionException(BaseErrorCode code) {
        super(code);
    }
}
