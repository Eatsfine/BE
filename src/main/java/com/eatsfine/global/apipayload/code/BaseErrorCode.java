package com.eatsfine.global.apipayload.code;

public interface BaseErrorCode {
    ErrorReasonDto getReason();
    ErrorReasonDto getReasonHttpStatus();
}
