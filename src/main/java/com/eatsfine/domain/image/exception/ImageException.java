package com.eatsfine.domain.image.exception;


import com.eatsfine.global.apipayload.code.BaseErrorCode;
import com.eatsfine.global.apipayload.exception.GeneralException;

public class ImageException extends GeneralException {
    public ImageException(BaseErrorCode code) {
        super(code);
    }
}
