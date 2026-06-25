package com.board.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    // 비즈니스 예외 처리 (존재하지 않는 게시글, 권한 없음 등)
    @ExceptionHandler(IllegalArgumentException.class)
    public String handleIllegalArgument(IllegalArgumentException e, Model model) {
        log.warn("비즈니스 예외 발생: {}", e.getMessage());
        model.addAttribute("errorMsg", e.getMessage());
        return "error/business-error";
    }

    // 예상치 못한 서버 오류
    @ExceptionHandler(Exception.class)
    public String handleException(Exception e, Model model) {
        log.error("서버 오류 발생", e);
        model.addAttribute("errorMsg", "일시적인 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
        return "error/server-error";
    }
}
