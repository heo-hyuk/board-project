package com.board.controller;

import com.board.dto.UserJoinDto;
import com.board.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    // 로그인 페이지
    @GetMapping("/login")
    public String loginPage(@RequestParam(required = false) String error, Model model) {
        if (error != null) {
            model.addAttribute("errorMsg", "아이디 또는 비밀번호가 올바르지 않습니다.");
        }
        return "auth/login";
    }

    // 회원가입 페이지
    @GetMapping("/join")
    public String joinPage(Model model) {
        model.addAttribute("userJoinDto", new UserJoinDto());
        return "auth/join";
    }

    // 회원가입 처리
    @PostMapping("/join")
    public String join(@Valid @ModelAttribute UserJoinDto dto,
                       BindingResult bindingResult,
                       RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "auth/join";
        }
        try {
            userService.join(dto);
            redirectAttributes.addFlashAttribute("successMsg", "회원가입이 완료되었습니다. 로그인해주세요.");
            return "redirect:/auth/login";
        } catch (IllegalArgumentException e) {
            bindingResult.reject("joinFail", e.getMessage());
            return "auth/join";
        }
    }
}
