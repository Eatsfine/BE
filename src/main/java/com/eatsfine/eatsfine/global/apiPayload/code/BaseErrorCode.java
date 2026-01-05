package com.eatsfine.eatsfine.global.apiPayload.code;

public interface BaseErrorCode {
    ErrorReasonDto getReason();
    ErrorReasonDto getReasonHttpStatus();
}
