package com.eatsfine.eatsfine.global.apiPayload.handler;

import com.eatsfine.eatsfine.domain.user.status.AuthErrorStatus;
import com.eatsfine.eatsfine.global.apiPayload.ApiResponse;
import com.eatsfine.eatsfine.global.apiPayload.code.BaseErrorCode;
import com.eatsfine.eatsfine.global.apiPayload.code.status.ErrorStatus;
import com.eatsfine.eatsfine.global.apiPayload.exception.GeneralException;
import jakarta.validation.ConstraintViolationException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Slf4j
@RestControllerAdvice
public class GeneralExceptionAdvice extends ResponseEntityExceptionHandler {

    // 1. 커스텀 예외(GeneralException) 처리
    @ExceptionHandler(value = GeneralException.class)
    public ResponseEntity<Object> onGeneralException(GeneralException exception, HttpServletRequest request) {
        // exception.getCode()는 BaseErrorCode 인터페이스 타입이므로 onFailure에 바로 전달 가능합니다.
        return handleExceptionInternal(exception, exception.getCode(), null, request);
    }

    // 2. 일반적인 모든 예외 처리 (정의되지 않은 에러)
    @ExceptionHandler
    public ResponseEntity<Object> exception(Exception e, WebRequest request) {
        log.error("Unhandled Exception 발생: ", e);
        // 시스템 전체 공통 에러인 _INTERNAL_SERVER_ERROR를 사용합니다.
        return handleExceptionInternalFalse(e, ErrorStatus._INTERNAL_SERVER_ERROR, HttpHeaders.EMPTY, ErrorStatus._INTERNAL_SERVER_ERROR.getHttpStatus(), request, e.getMessage());
    }

    // @Valid 유효성 검사 실패 시 (RequestBody)
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException e,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request
    ) {
        String errorMessage = e.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        return handleExceptionInternalFalse(e, ErrorStatus._BAD_REQUEST, headers, status, request, errorMessage);
    }

    // @RequestParam, @PathVariable 유효성 검사 실패 시 (ConstraintViolationException)
    @ExceptionHandler
    public ResponseEntity<Object> validation(ConstraintViolationException e, WebRequest request) {
        // 첫 번째 에러 메시지만 추출
        String errorMessage = e.getConstraintViolations().iterator().next().getMessage();
        return handleExceptionInternalFalse(e, ErrorStatus._BAD_REQUEST, HttpHeaders.EMPTY, ErrorStatus._BAD_REQUEST.getHttpStatus(), request, errorMessage);
    }

    // @PreAuthorize 권한 실패 시 발생하는 예외 캐치
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Object> handleAccessDeniedException(AccessDeniedException e, WebRequest request) {
        log.warn("Access Denied: {}", e.getMessage());

        // 2. 기존 메서드를 활용해 ResponseEntity<Object>로 반환
        return handleExceptionInternalFalse(
                e,
                AuthErrorStatus.FORBIDDEN_OWNER,
                HttpHeaders.EMPTY,
                AuthErrorStatus.FORBIDDEN_OWNER.getHttpStatus(),
                request,
                null
        );
    }

    // 3. 커스텀 예외용 내부 응답 생성 메서드
    private ResponseEntity<Object> handleExceptionInternal(Exception e, BaseErrorCode code, HttpHeaders headers, HttpServletRequest request) {
        // 정의하신 ApiResponse.onFailure(BaseErrorCode code, T result)를 호출합니다.
        ApiResponse<Object> body = ApiResponse.onFailure(code, null);
        WebRequest webRequest = new ServletWebRequest(request);
        return super.handleExceptionInternal(e, body, headers, code.getReasonHttpStatus().getHttpStatus(), webRequest);
    }

    // 4. 일반 예외용 내부 응답 생성 메서드
    private ResponseEntity<Object> handleExceptionInternalFalse(Exception e, BaseErrorCode code, HttpHeaders headers, HttpStatusCode status, WebRequest request, String errorPoint) {
        // 일반 예외의 경우 errorPoint(메시지)를 result에 담아 전달할 수 있습니다.
        ApiResponse<Object> body = ApiResponse.onFailure(code, errorPoint);
        return super.handleExceptionInternal(e, body, headers, status, request);
    }
}