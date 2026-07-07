package com.board.api.post;

import com.board.api.common.ApiResponse;
import com.board.domain.Post;
import com.board.dto.PostDto;
import com.board.dto.PostSearchDto;
import com.board.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
@Tag(name = "Post", description = "게시글 API")
public class PostApiController {

    private final PostService postService;

    // GET /api/v1/posts?keyword=&category=&page=1&pageSize=9
    @GetMapping
    @Operation(summary = "게시글 목록 조회", description = "키워드/카테고리 검색 + 페이지네이션")
    public ApiResponse<Map<String, Object>> list(@ModelAttribute PostSearchDto searchDto) {
        Map<String, Object> result = postService.searchPosts(searchDto);

        // PostDto 목록을 PostResponse로 변환
        @SuppressWarnings("unchecked")
        List<PostDto> posts = (List<PostDto>) result.get("posts");
        List<PostResponse> postResponses = posts.stream()
                .map(PostResponse::from)
                .collect(Collectors.toList());

        result.put("posts", postResponses);
        return ApiResponse.ok(result);
    }

    // GET /api/v1/posts/{id}
    @GetMapping("/{id}")
    @Operation(summary = "게시글 상세 조회")
    public ApiResponse<PostResponse> detail(@PathVariable Long id) {
        Post post = postService.findById(id);
        int likeCount = postService.getLikeCount(id);
        return ApiResponse.ok(PostResponse.from(post, likeCount));
    }

    // POST /api/v1/posts
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "게시글 작성", description = "로그인 필요")
    public ApiResponse<Long> create(@Valid @RequestBody PostCreateRequest request,
                                    @AuthenticationPrincipal UserDetails userDetails) {
        Long postId = postService.write(
                request.getTitle(),
                request.getSummary(),
                request.getContent(),
                request.getCategory(),
                request.getTags(),
                userDetails.getUsername()
        );
        return ApiResponse.ok(postId);
    }

    // PUT /api/v1/posts/{id}
    @PutMapping("/{id}")
    @Operation(summary = "게시글 수정", description = "작성자만 가능")
    public ApiResponse<Void> update(@PathVariable Long id,
                                    @Valid @RequestBody PostUpdateRequest request,
                                    @AuthenticationPrincipal UserDetails userDetails) {
        postService.update(
                id,
                request.getTitle(),
                request.getSummary(),
                request.getContent(),
                request.getCategory(),
                request.getTags(),
                userDetails.getUsername()
        );
        return ApiResponse.ok("수정되었습니다.");
    }

    // DELETE /api/v1/posts/{id}
    @DeleteMapping("/{id}")
    @Operation(summary = "게시글 삭제", description = "작성자만 가능")
    public ApiResponse<Void> delete(@PathVariable Long id,
                                    @AuthenticationPrincipal UserDetails userDetails) {
        postService.delete(id, userDetails.getUsername());
        return ApiResponse.ok("삭제되었습니다.");
    }
}
