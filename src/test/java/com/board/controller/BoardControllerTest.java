package com.board.controller;

import com.board.config.SecurityConfig;
import com.board.domain.Post;
import com.board.domain.User;
import com.board.mapper.PostMapper;
import com.board.security.CustomUserDetailsService;
import com.board.service.CommentService;
import com.board.service.PostService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// BoardController 웹 레이어만 로드
// SecurityConfig는 @WebMvcTest 타입 필터에 포함 안 되므로 @Import 명시 필요
@WebMvcTest(BoardController.class)
@Import(SecurityConfig.class)
class BoardControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockBean private PostService postService;
    @MockBean private CommentService commentService;
    // SecurityConfig가 CustomUserDetailsService, JwtAuthenticationFilter를 주입하므로 @MockBean 필수
    @MockBean private CustomUserDetailsService customUserDetailsService;
    @MockBean private com.board.security.JwtAuthenticationFilter jwtAuthenticationFilter;
    // @MapperScan으로 등록된 PostMapper가 SqlSessionFactory 없이 실패하는 것을 방지
    @MockBean private PostMapper postMapper;

    // 테스트용 게시글 생성 헬퍼
    private Post createPost(String authorUsername) {
        User author = User.create(authorUsername, "pw", authorUsername + "@test.com", "작성자");
        Post post = Post.create("테스트 제목", "요약", "본문 내용", "Java", author);
        ReflectionTestUtils.setField(post, "id", 1L);
        return post;
    }

    // 테스트용 searchPosts 결과 Map 생성 헬퍼
    private Map<String, Object> emptySearchResult() {
        Map<String, Object> result = new HashMap<>();
        result.put("posts", List.of());
        result.put("totalCount", 0);
        result.put("totalPages", 0);
        result.put("currentPage", 1);
        return result;
    }

    @Test
    @DisplayName("GET /board - 게시글 목록 페이지 반환")
    void list() throws Exception {
        // given
        given(postService.searchPosts(any())).willReturn(emptySearchResult());

        // when & then
        mockMvc.perform(get("/board"))
            .andExpect(status().isOk())
            .andExpect(view().name("board/list"))
            .andExpect(model().attributeExists("posts", "totalCount", "totalPages", "currentPage"));
    }

    @Test
    @DisplayName("GET /board - 비로그인 사용자도 목록 조회 가능")
    void list_anonymous() throws Exception {
        // given
        given(postService.searchPosts(any())).willReturn(emptySearchResult());

        // when & then: 인증 없이도 200 OK
        mockMvc.perform(get("/board"))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /board/{id} - 비로그인 사용자도 게시글 상세 조회 가능")
    void detail_anonymous() throws Exception {
        // given
        Post post = createPost("author");
        given(postService.findById(1L)).willReturn(post);
        given(commentService.findByPost(1L)).willReturn(List.of());
        given(postService.getLikeCount(1L)).willReturn(5);

        // when & then
        mockMvc.perform(get("/board/1"))
            .andExpect(status().isOk())
            .andExpect(view().name("board/detail"))
            .andExpect(model().attributeExists("post", "comments", "likeCount"));
    }

    @Test
    @DisplayName("GET /board/{id} - 로그인 사용자는 isAuthor, liked 정보 추가")
    @WithMockUser(username = "author")
    void detail_loggedIn() throws Exception {
        // given
        Post post = createPost("author");
        given(postService.findById(1L)).willReturn(post);
        given(commentService.findByPost(1L)).willReturn(List.of());
        given(postService.getLikeCount(1L)).willReturn(0);
        given(postService.isLikedByUser(eq(1L), eq("author"))).willReturn(false);

        // when & then
        mockMvc.perform(get("/board/1"))
            .andExpect(status().isOk())
            .andExpect(model().attribute("isAuthor", true))    // 작성자 본인
            .andExpect(model().attribute("liked", false));
    }

    @Test
    @DisplayName("GET /board/{id} - 로그인했지만 작성자가 아닌 경우 isAuthor=false")
    @WithMockUser(username = "otheruser")
    void detail_loggedIn_notAuthor() throws Exception {
        // given
        Post post = createPost("author");
        given(postService.findById(1L)).willReturn(post);
        given(commentService.findByPost(1L)).willReturn(List.of());
        given(postService.getLikeCount(1L)).willReturn(0);
        given(postService.isLikedByUser(eq(1L), eq("otheruser"))).willReturn(false);

        // when & then
        mockMvc.perform(get("/board/1"))
            .andExpect(status().isOk())
            .andExpect(model().attribute("isAuthor", false)); // 작성자 아님
    }

    @Test
    @DisplayName("GET /board/write - 비로그인 시 로그인 페이지로 리다이렉트")
    void writePage_unauthorized() throws Exception {
        mockMvc.perform(get("/board/write"))
            .andExpect(status().is3xxRedirection()); // Spring Security가 로그인으로 리다이렉트
    }

    @Test
    @DisplayName("GET /board/write - 로그인 시 작성 페이지 반환")
    @WithMockUser(username = "testuser")
    void writePage_authorized() throws Exception {
        mockMvc.perform(get("/board/write"))
            .andExpect(status().isOk())
            .andExpect(view().name("board/write"));
    }

    @Test
    @DisplayName("POST /board/{id}/delete - 로그인 후 삭제 성공")
    @WithMockUser(username = "testuser")
    void delete_success() throws Exception {
        // given
        doNothing().when(postService).delete(eq(1L), eq("testuser"));

        // when & then
        mockMvc.perform(post("/board/1/delete").with(csrf()))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/board"));
    }

    @Test
    @DisplayName("POST /board/{id}/delete - CSRF 토큰 없으면 403 Forbidden")
    @WithMockUser(username = "testuser")
    void delete_withoutCsrf_forbidden() throws Exception {
        mockMvc.perform(post("/board/1/delete"))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /board/{id}/delete - 비로그인 시 리다이렉트")
    void delete_unauthorized() throws Exception {
        mockMvc.perform(post("/board/1/delete").with(csrf()))
            .andExpect(status().is3xxRedirection());
    }
}
