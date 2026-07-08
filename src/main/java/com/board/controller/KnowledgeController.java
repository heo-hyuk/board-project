package com.board.controller;

import com.board.dto.KnowledgePostDto;
import com.board.dto.KnowledgeSearchDto;
import com.board.service.KnowledgePostService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

@Controller
@RequestMapping("/knowledge")
@RequiredArgsConstructor
public class KnowledgeController {

    private final KnowledgePostService knowledgePostService;

    // 글 목록
    @GetMapping
    public String list(@ModelAttribute KnowledgeSearchDto searchDto, Model model) {
        Map<String, Object> result = knowledgePostService.getRootPosts(searchDto);
        model.addAttribute("posts", result.get("posts"));
        model.addAttribute("totalCount", result.get("totalCount"));
        model.addAttribute("totalPages", result.get("totalPages"));
        model.addAttribute("currentPage", result.get("currentPage"));
        model.addAttribute("searchDto", searchDto);
        return "knowledge/list";
    }

    // 글 상세 + 브랜치 트리
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id,
                         @AuthenticationPrincipal UserDetails userDetails,
                         Model model) {
        KnowledgePostDto post = knowledgePostService.findById(id);
        model.addAttribute("post", post);
        model.addAttribute("branches", knowledgePostService.getBranchesDfs(id));

        String username = (userDetails != null) ? userDetails.getUsername() : null;
        model.addAttribute("loginUsername", username);
        model.addAttribute("isAuthor", username != null && username.equals(post.getUsername()));
        return "knowledge/detail";
    }

    // 새 원본 글 작성 폼
    @GetMapping("/write")
    public String writePage() {
        return "knowledge/write";
    }

    // 새 원본 글 작성 처리
    @PostMapping("/write")
    public String write(@RequestParam String title,
                        @RequestParam String content,
                        @AuthenticationPrincipal UserDetails userDetails,
                        RedirectAttributes redirectAttributes,
                        Model model) {
        if (title.isBlank() || title.length() > 200) {
            model.addAttribute("errorMsg", "제목은 1~200자 이내로 입력해주세요.");
            return "knowledge/write";
        }
        if (content.isBlank()) {
            model.addAttribute("errorMsg", "내용을 입력해주세요.");
            return "knowledge/write";
        }
        Long postId = knowledgePostService.write(title.trim(), content, userDetails.getUsername());
        redirectAttributes.addFlashAttribute("successMsg", "글이 작성되었습니다.");
        return "redirect:/knowledge/" + postId;
    }

    // 브랜치 작성 폼 (부모 내용 미리채움)
    @GetMapping("/{parentId}/branch")
    public String branchPage(@PathVariable Long parentId, Model model) {
        KnowledgePostDto parent = knowledgePostService.findByIdReadOnly(parentId);
        model.addAttribute("parent", parent);
        return "knowledge/branch";
    }

    // 브랜치 생성 처리
    @PostMapping("/{parentId}/branch")
    public String branch(@PathVariable Long parentId,
                         @RequestParam String title,
                         @RequestParam String content,
                         @RequestParam(required = false) String branchName,
                         @AuthenticationPrincipal UserDetails userDetails,
                         RedirectAttributes redirectAttributes,
                         Model model) {
        if (title.isBlank() || title.length() > 200) {
            model.addAttribute("errorMsg", "제목은 1~200자 이내로 입력해주세요.");
            model.addAttribute("parent", knowledgePostService.findByIdReadOnly(parentId));
            return "knowledge/branch";
        }
        if (content.isBlank()) {
            model.addAttribute("errorMsg", "내용을 입력해주세요.");
            model.addAttribute("parent", knowledgePostService.findByIdReadOnly(parentId));
            return "knowledge/branch";
        }
        Long rootId = knowledgePostService.branch(
                parentId, title.trim(), content,
                (branchName != null && !branchName.isBlank()) ? branchName.trim() : null,
                userDetails.getUsername()
        );
        redirectAttributes.addFlashAttribute("successMsg", "브랜치가 등록되었습니다.");
        return "redirect:/knowledge/" + rootId;
    }

    // 삭제 처리
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id,
                         @AuthenticationPrincipal UserDetails userDetails,
                         RedirectAttributes redirectAttributes) {
        Long rootId = knowledgePostService.delete(id, userDetails.getUsername());
        redirectAttributes.addFlashAttribute("successMsg", "삭제되었습니다.");
        return rootId != null ? "redirect:/knowledge/" + rootId : "redirect:/knowledge";
    }
}
