package com.eatsfine.eatsfine.domain.image.exception;


import com.eatsfine.eatsfine.global.apiPayload.code.BaseErrorCode;
import com.eatsfine.eatsfine.global.apiPayload.exception.GeneralException;

public class ImageException extends GeneralException {
    public ImageException(BaseErrorCode code) {
        super(code);
    }
}
