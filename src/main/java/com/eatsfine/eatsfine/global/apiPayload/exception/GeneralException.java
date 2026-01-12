package com.eatsfine.eatsfine.global.apiPayload.exception;

import com.eatsfine.eatsfine.global.apiPayload.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

// 프로젝트 Exception
@Getter
@AllArgsConstructor
public class GeneralException extends RuntimeException {
    private final BaseErrorCode code;
}
