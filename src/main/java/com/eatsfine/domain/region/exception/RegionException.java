package com.eatsfine.domain.region.exception;

import com.eatsfine.global.apipayload.code.BaseErrorCode;
import com.eatsfine.global.apipayload.exception.GeneralException;

public class RegionException extends GeneralException {
    public RegionException(BaseErrorCode code) {
        super(code);
    }
}
