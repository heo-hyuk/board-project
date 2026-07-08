package com.board.config;

import com.board.security.CustomUserDetailsService;
import com.board.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.http.HttpStatus;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    // MVC 체인에서만 사용 (JwtAuthenticationFilter는 apiFilterChain 메서드 파라미터로 주입)
    private final CustomUserDetailsService userDetailsService;

    // JwtAuthenticationFilter를 서블릿 필터로 자동 등록되지 않도록 방지
    // Spring Security filterChain에서만 수동으로 사용 (addFilterBefore)
    @Bean
    public FilterRegistrationBean<JwtAuthenticationFilter> jwtFilterRegistration(JwtAuthenticationFilter filter) {
        FilterRegistrationBean<JwtAuthenticationFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false); // 자동 등록 비활성화
        return registration;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    // API 전용 FilterChain (우선순위 높음) - Stateless + JWT
    // JwtAuthenticationFilter를 메서드 파라미터로 받아 테스트 시 @MockBean으로 대체 가능
    @Bean
    @Order(1)
    public SecurityFilterChain apiFilterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {
        http
            .securityMatcher("/api/**")
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .authorizeHttpRequests(auth -> auth
                // 로그인/토큰 갱신은 인증 불필요
                .requestMatchers(HttpMethod.POST, "/api/v1/auth/login", "/api/v1/auth/refresh").permitAll()
                // 게시글/댓글 읽기 공개
                .requestMatchers(HttpMethod.GET, "/api/v1/posts", "/api/v1/posts/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/comments").permitAll()
                // 나머지 API는 JWT 인증 필요
                .anyRequest().authenticated()
            )
            // 인증 실패 시 302 리다이렉트 대신 401 반환
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
            )
            // JWT 필터를 UsernamePasswordAuthenticationFilter 앞에 추가
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // MVC 전용 FilterChain (우선순위 낮음) - Stateful + 세션 로그인
    @Bean
    @Order(2)
    public SecurityFilterChain mvcFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                // 비로그인 허용 경로
                .requestMatchers("/", "/auth/**", "/css/**", "/js/**", "/favicon.svg").permitAll()
                .requestMatchers("/board").permitAll()
                .requestMatchers("/board/{id:[0-9]+}").permitAll()
                .requestMatchers("/knowledge", "/knowledge/{id:[0-9]+}").permitAll()
                // Swagger UI
                .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                // 게시글 작성/수정/삭제, 댓글, 좋아요, 마이페이지는 로그인 필요
                .requestMatchers("/board/write", "/board/*/edit", "/board/*/delete").authenticated()
                .requestMatchers("/comment/**", "/user/**", "/like/**").authenticated()
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/auth/login")
                .loginProcessingUrl("/auth/login")
                .defaultSuccessUrl("/board", true)
                .failureUrl("/auth/login?error=true")
                .usernameParameter("username")
                .passwordParameter("password")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/auth/logout")
                .logoutSuccessUrl("/board")
                .permitAll()
            )
            .userDetailsService(userDetailsService);

        return http.build();
    }
}
