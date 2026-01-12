package com.eatsfine.eatsfine.global.apiPayload.exception;

import com.eatsfine.eatsfine.global.apiPayload.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GeneralException extends RuntimeException {

    private final BaseErrorCode code;
}
