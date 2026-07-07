package com.board.controller;

import com.board.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/comment")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    // 댓글 작성
    @PostMapping("/write")
    public String write(@RequestParam Long postId,
                        @RequestParam String content,
                        @AuthenticationPrincipal UserDetails userDetails) {
        // 서버 측 검증: 빈 값, 길이 초과
        if (content == null || content.isBlank()) {
            return "redirect:/board/" + postId;
        }
        if (content.length() > 500) {
            return "redirect:/board/" + postId;
        }
        commentService.write(postId, content.trim(), userDetails.getUsername());
        return "redirect:/board/" + postId;
    }

    // 댓글 삭제
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id,
                         @RequestParam Long postId,
                         @AuthenticationPrincipal UserDetails userDetails) {
        commentService.delete(id, userDetails.getUsername());
        return "redirect:/board/" + postId;
    }
}
