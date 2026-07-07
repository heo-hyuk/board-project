package com.board.exception;

// 리소스를 찾을 수 없을 때 사용하는 예외 (HTTP 404)
// 기존 IllegalArgumentException을 쓰면 400이 반환되어 의미론적으로 부정확
public class NotFoundException extends RuntimeException {

    public NotFoundException(String message) {
        super(message);
    }
}
