package com.eatsfine.eatsfine.global.apiPayload.handler;

import com.eatsfine.eatsfine.global.apiPayload.ApiResponse;
import com.eatsfine.eatsfine.global.apiPayload.exception.GeneralException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GeneralExceptionAdvice {

    // 커스텀 예외 처리
    @ExceptionHandler(GeneralException.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneralException(GeneralException e) {
        return ResponseEntity.status(e.getCode().getReasonHttpStatus().getHttpStatus())
                .body(ApiResponse.onFailure(e.getCode(), null));

    }
}
