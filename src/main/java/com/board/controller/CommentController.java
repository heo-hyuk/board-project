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
        commentService.write(postId, content, userDetails.getUsername());
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
