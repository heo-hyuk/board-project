package com.board.api.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

// API 패키지 전용 예외 핸들러 (기존 GlobalExceptionHandler와 충돌 없음)
@Slf4j
@RestControllerAdvice(basePackages = "com.board.api")
public class ApiExceptionHandler {

    // 비즈니스 예외 (존재하지 않는 게시글, 권한 없음 등)
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleIllegalArgument(IllegalArgumentException e) {
        log.warn("API 비즈니스 예외: {}", e.getMessage());
        return ApiResponse.fail(e.getMessage());
    }

    // 접근 권한 없음
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ApiResponse<Void> handleAccessDenied(AccessDeniedException e) {
        return ApiResponse.fail("접근 권한이 없습니다.");
    }

    // @Valid 검증 실패
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleValidation(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        return ApiResponse.fail(message);
    }

    // DB 제약 조건 위반 (중복 등)
    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiResponse<Void> handleDataIntegrity(DataIntegrityViolationException e) {
        log.warn("API DB 제약 조건 위반: {}", e.getMessage());
        return ApiResponse.fail("이미 사용 중인 값입니다.");
    }

    // 서버 내부 오류
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<Void> handleException(Exception e) {
        log.error("API 서버 오류", e);
        return ApiResponse.fail("서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
    }
}
