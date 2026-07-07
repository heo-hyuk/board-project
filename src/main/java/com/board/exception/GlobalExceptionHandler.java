package com.board.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    // 리소스를 찾을 수 없음 (게시글, 댓글 등 not found)
    @ExceptionHandler(NotFoundException.class)
    public String handleNotFound(NotFoundException e, Model model) {
        log.warn("리소스 없음: {}", e.getMessage());
        model.addAttribute("errorMsg", e.getMessage());
        return "error/business-error";
    }

    // 비즈니스 예외 처리 (권한 없음, 잘못된 요청 등)
    @ExceptionHandler(IllegalArgumentException.class)
    public String handleIllegalArgument(IllegalArgumentException e, Model model) {
        log.warn("비즈니스 예외 발생: {}", e.getMessage());
        model.addAttribute("errorMsg", e.getMessage());
        return "error/business-error";
    }

    // DB 제약 조건 위반 (중복 닉네임/이메일 등)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public String handleDataIntegrity(DataIntegrityViolationException e, Model model) {
        log.warn("DB 제약 조건 위반: {}", e.getMessage());
        model.addAttribute("errorMsg", "이미 사용 중인 값입니다. 다른 값을 입력해주세요.");
        return "error/business-error";
    }

    // 정적 리소스 404 (favicon.ico 등) - 로그 없이 무시
    @ExceptionHandler(NoResourceFoundException.class)
    public String handleNoResource(NoResourceFoundException e) {
        return "redirect:/";
    }

    // 예상치 못한 서버 오류
    @ExceptionHandler(Exception.class)
    public String handleException(Exception e, Model model) {
        log.error("서버 오류 발생", e);
        model.addAttribute("errorMsg", "일시적인 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
        return "error/server-error";
    }
}
