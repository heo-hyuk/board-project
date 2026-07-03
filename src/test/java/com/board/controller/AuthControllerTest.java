package com.board.controller;

import com.board.config.SecurityConfig;
import com.board.mapper.PostMapper;
import com.board.security.CustomUserDetailsService;
import com.board.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// AuthController 웹 레이어만 로드 (DB 불필요)
// SecurityConfig는 @WebMvcTest 타입 필터에 포함 안 되므로 @Import 명시 필요
@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
class AuthControllerTest {

    @Autowired private MockMvc mockMvc;

    // SecurityConfig가 CustomUserDetailsService를 주입하므로 @MockBean 필수
    @MockBean private CustomUserDetailsService customUserDetailsService;
    @MockBean private UserService userService;
    // @MapperScan으로 등록된 PostMapper가 SqlSessionFactory 없이 실패하는 것을 방지
    @MockBean private PostMapper postMapper;

    @Test
    @DisplayName("GET /auth/login - 로그인 페이지 반환")
    void loginPage() throws Exception {
        mockMvc.perform(get("/auth/login"))
            .andExpect(status().isOk())
            .andExpect(view().name("auth/login"));
    }

    @Test
    @DisplayName("GET /auth/login?error - 에러 메시지 모델에 추가")
    void loginPage_withError() throws Exception {
        mockMvc.perform(get("/auth/login").param("error", ""))
            .andExpect(status().isOk())
            .andExpect(model().attributeExists("errorMsg"));
    }

    @Test
    @DisplayName("GET /auth/join - 회원가입 페이지 반환 + DTO 모델 바인딩")
    void joinPage() throws Exception {
        mockMvc.perform(get("/auth/join"))
            .andExpect(status().isOk())
            .andExpect(view().name("auth/join"))
            .andExpect(model().attributeExists("userJoinDto"));
    }

    @Test
    @DisplayName("POST /auth/join - 유효성 검사 실패 (짧은 아이디, 잘못된 이메일)")
    void join_validationFail() throws Exception {
        mockMvc.perform(post("/auth/join").with(csrf())
            .param("username", "ab")         // @Size(min=4) 위반
            .param("password", "123")         // @Size(min=6) 위반
            .param("email", "notanemail")     // @Email 위반
            .param("nickname", "닉"))
            .andExpect(status().isOk())
            .andExpect(view().name("auth/join")); // 에러 → 다시 폼으로
    }

    @Test
    @DisplayName("POST /auth/join - 성공 시 로그인 페이지로 리다이렉트")
    void join_success() throws Exception {
        doNothing().when(userService).join(any());

        mockMvc.perform(post("/auth/join").with(csrf())
            .param("username", "testuser")
            .param("password", "password123")
            .param("email", "test@test.com")
            .param("nickname", "테스터"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/auth/login"));
    }

    @Test
    @DisplayName("POST /auth/join - 중복 아이디 → 폼으로 복귀")
    void join_duplicateUsername() throws Exception {
        doThrow(new IllegalArgumentException("이미 사용 중인 아이디입니다."))
            .when(userService).join(any());

        mockMvc.perform(post("/auth/join").with(csrf())
            .param("username", "duplicate")
            .param("password", "password123")
            .param("email", "dup@test.com")
            .param("nickname", "중복유저"))
            .andExpect(status().isOk())
            .andExpect(view().name("auth/join")); // 에러 → 다시 폼으로
    }

    @Test
    @DisplayName("POST /auth/join - CSRF 토큰 없으면 403 Forbidden")
    void join_withoutCsrf_forbidden() throws Exception {
        mockMvc.perform(post("/auth/join")
            .param("username", "testuser")
            .param("password", "password123")
            .param("email", "test@test.com")
            .param("nickname", "테스터"))
            .andExpect(status().isForbidden());
    }
}
