package com.board.controller;

import com.board.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/like")
@RequiredArgsConstructor
public class LikeController {

    private final PostService postService;

    // 좋아요 토글 (AJAX)
    @PostMapping("/{postId}")
    public Map<String, Object> toggleLike(@PathVariable Long postId,
                                          @AuthenticationPrincipal UserDetails userDetails) {
        boolean liked = postService.toggleLike(postId, userDetails.getUsername());
        int likeCount = postService.getLikeCount(postId);
        return Map.of("liked", liked, "likeCount", likeCount);
    }
}
