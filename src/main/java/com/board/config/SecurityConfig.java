package com.board.config;

import com.board.security.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                // 비로그인 허용 경로
                .requestMatchers("/", "/auth/**", "/css/**", "/js/**").permitAll()
                .requestMatchers("/board").permitAll()
                .requestMatchers("/board/{id:[0-9]+}").permitAll()  // 게시글 상세 조회만 허용
                // 게시글 작성/수정/삭제, 댓글, 좋아요, 마이페이지는 로그인 필요
                .requestMatchers("/board/write", "/board/*/edit", "/board/*/delete").authenticated()
                .requestMatchers("/comment/**", "/user/**", "/like/**").authenticated()
                // 나머지는 로그인 필요
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/auth/login")          // 커스텀 로그인 페이지
                .loginProcessingUrl("/auth/login") // 로그인 처리 URL
                .defaultSuccessUrl("/board", true) // 로그인 성공 시 이동
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
