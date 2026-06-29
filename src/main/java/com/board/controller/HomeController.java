package com.board.controller;

import com.board.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final PostService postService;

    // 블로그 홈 페이지 (최신 포스트 6개 표시)
    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("recentPosts", postService.getRecentPosts());
        return "home";
    }
}
