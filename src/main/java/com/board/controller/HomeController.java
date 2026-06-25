package com.board.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    // 루트 접속 시 게시판 목록으로 이동
    @GetMapping("/")
    public String home() {
        return "redirect:/board";
    }
}
