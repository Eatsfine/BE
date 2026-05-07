package com.eatsfine.global.apipayload.exception;

import com.eatsfine.global.apipayload.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
public class GeneralException extends RuntimeException {

    private final BaseErrorCode code;

    public GeneralException(BaseErrorCode code) {
        super(code.getReason().getMessage());
        this.code = code;
    }

    public GeneralException(BaseErrorCode code, Throwable cause) {
        super(code.getReason().getMessage(), cause);
        this.code = code;
    }
}
