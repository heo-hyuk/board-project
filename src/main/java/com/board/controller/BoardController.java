package com.board.controller;

import com.board.domain.Post;
import com.board.dto.PostSearchDto;
import com.board.service.CommentService;
import com.board.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

@Controller
@RequestMapping("/board")
@RequiredArgsConstructor
public class BoardController {

    private final PostService postService;
    private final CommentService commentService;

    // 포스트 목록 (검색 + 페이지네이션)
    @GetMapping
    public String list(@ModelAttribute PostSearchDto searchDto, Model model) {
        Map<String, Object> result = postService.searchPosts(searchDto);
        model.addAttribute("posts", result.get("posts"));
        model.addAttribute("totalCount", result.get("totalCount"));
        model.addAttribute("totalPages", result.get("totalPages"));
        model.addAttribute("currentPage", result.get("currentPage"));
        model.addAttribute("searchDto", searchDto);
        return "board/list";
    }

    // 포스트 상세
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id,
                         @AuthenticationPrincipal UserDetails userDetails,
                         Model model) {
        Post post = postService.findById(id);
        model.addAttribute("post", post);
        model.addAttribute("comments", commentService.findByPost(id));
        model.addAttribute("likeCount", postService.getLikeCount(id));

        String username = (userDetails != null) ? userDetails.getUsername() : null;
        if (username != null) {
            model.addAttribute("isAuthor", post.getUser().getUsername().equals(username));
            model.addAttribute("loginUsername", username);
            model.addAttribute("liked", postService.isLikedByUser(id, username));
        } else {
            model.addAttribute("liked", false);
        }
        return "board/detail";
    }

    // 포스트 작성 페이지
    @GetMapping("/write")
    public String writePage() {
        return "board/write";
    }

    // 포스트 작성 처리
    @PostMapping("/write")
    public String write(@RequestParam String title,
                        @RequestParam(required = false) String summary,
                        @RequestParam String content,
                        @RequestParam(defaultValue = "Java") String category,
                        @RequestParam(required = false) String tags,
                        @AuthenticationPrincipal UserDetails userDetails,
                        RedirectAttributes redirectAttributes,
                        Model model) {
        if (title.isBlank() || title.length() > 200) {
            model.addAttribute("errorMsg", "제목은 1~200자 이내로 입력해주세요.");
            return "board/write";
        }
        if (content.isBlank()) {
            model.addAttribute("errorMsg", "내용을 입력해주세요.");
            return "board/write";
        }
        Long postId = postService.write(title.trim(), summary, content, category, tags, userDetails.getUsername());
        redirectAttributes.addFlashAttribute("successMsg", "포스트가 작성되었습니다.");
        return "redirect:/board/" + postId;
    }

    // 포스트 수정 페이지
    @GetMapping("/{id}/edit")
    public String editPage(@PathVariable Long id,
                           @AuthenticationPrincipal UserDetails userDetails,
                           Model model) {
        Post post = postService.findByIdReadOnly(id);
        if (!post.getUser().getUsername().equals(userDetails.getUsername())) {
            return "redirect:/board/" + id;
        }
        model.addAttribute("post", post);
        return "board/edit";
    }

    // 포스트 수정 처리
    @PostMapping("/{id}/edit")
    public String edit(@PathVariable Long id,
                       @RequestParam String title,
                       @RequestParam(required = false) String summary,
                       @RequestParam String content,
                       @RequestParam(defaultValue = "Java") String category,
                       @RequestParam(required = false) String tags,
                       @AuthenticationPrincipal UserDetails userDetails,
                       RedirectAttributes redirectAttributes,
                       Model model) {
        if (title.isBlank() || title.length() > 200) {
            model.addAttribute("errorMsg", "제목은 1~200자 이내로 입력해주세요.");
            model.addAttribute("post", postService.findByIdReadOnly(id));
            return "board/edit";
        }
        if (content.isBlank()) {
            model.addAttribute("errorMsg", "내용을 입력해주세요.");
            model.addAttribute("post", postService.findByIdReadOnly(id));
            return "board/edit";
        }
        postService.update(id, title.trim(), summary, content, category, tags, userDetails.getUsername());
        redirectAttributes.addFlashAttribute("successMsg", "포스트가 수정되었습니다.");
        return "redirect:/board/" + id;
    }

    // 포스트 삭제 처리
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id,
                         @AuthenticationPrincipal UserDetails userDetails,
                         RedirectAttributes redirectAttributes) {
        postService.delete(id, userDetails.getUsername());
        redirectAttributes.addFlashAttribute("successMsg", "포스트가 삭제되었습니다.");
        return "redirect:/board";
    }
}
