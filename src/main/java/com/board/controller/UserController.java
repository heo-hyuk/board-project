package com.board.controller;

import com.board.service.PostService;
import com.board.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final PostService postService;

    // 마이페이지
    @GetMapping("/mypage")
    public String mypage(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        model.addAttribute("user", userService.findByUsername(userDetails.getUsername()));
        model.addAttribute("myPosts", postService.findMyPosts(userDetails.getUsername()));
        return "user/mypage";
    }

    // 닉네임 수정
    @PostMapping("/nickname")
    public String updateNickname(@RequestParam String nickname,
                                 @AuthenticationPrincipal UserDetails userDetails,
                                 RedirectAttributes redirectAttributes) {
        userService.updateNickname(userDetails.getUsername(), nickname);
        redirectAttributes.addFlashAttribute("successMsg", "닉네임이 변경되었습니다.");
        return "redirect:/user/mypage";
    }

    // 비밀번호 변경
    @PostMapping("/password")
    public String updatePassword(@RequestParam String currentPassword,
                                 @RequestParam String newPassword,
                                 @AuthenticationPrincipal UserDetails userDetails,
                                 RedirectAttributes redirectAttributes) {
        try {
            userService.updatePassword(userDetails.getUsername(), currentPassword, newPassword);
            redirectAttributes.addFlashAttribute("successMsg", "비밀번호가 변경되었습니다.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/user/mypage";
    }

    // 자기소개 변경
    @PostMapping("/bio")
    public String updateBio(@RequestParam String bio,
                            @AuthenticationPrincipal UserDetails userDetails,
                            RedirectAttributes redirectAttributes) {
        userService.updateBio(userDetails.getUsername(), bio);
        redirectAttributes.addFlashAttribute("successMsg", "소개가 변경되었습니다.");
        return "redirect:/user/mypage";
    }
}
