package com.board.api.comment;

import com.board.api.common.ApiResponse;
import com.board.dto.CommentDto;
import com.board.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/comments")
@RequiredArgsConstructor
@Tag(name = "Comment", description = "댓글 API")
public class CommentApiController {

    private final CommentService commentService;

    // GET /api/v1/comments?postId=1
    @GetMapping
    @Operation(summary = "댓글 목록 조회", description = "특정 게시글의 댓글 목록")
    public ApiResponse<List<CommentDto>> list(@RequestParam Long postId) {
        return ApiResponse.ok(commentService.findByPost(postId));
    }

    // POST /api/v1/comments
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "댓글 작성", description = "로그인 필요")
    public ApiResponse<Void> create(@Valid @RequestBody CommentCreateRequest request,
                                    @AuthenticationPrincipal UserDetails userDetails) {
        commentService.write(request.getPostId(), request.getContent(), userDetails.getUsername());
        return ApiResponse.ok("댓글이 작성되었습니다.");
    }

    // DELETE /api/v1/comments/{id}?postId=1
    @DeleteMapping("/{id}")
    @Operation(summary = "댓글 삭제", description = "작성자만 가능")
    public ApiResponse<Void> delete(@PathVariable Long id,
                                    @AuthenticationPrincipal UserDetails userDetails) {
        commentService.delete(id, userDetails.getUsername());
        return ApiResponse.ok("삭제되었습니다.");
    }
}
