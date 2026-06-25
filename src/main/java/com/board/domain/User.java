package com.board.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    private String username; // 로그인 아이디

    @Column(nullable = false)
    private String password;

    @Column(unique = true, nullable = false, length = 100)
    private String email;

    @Column(nullable = false, length = 50)
    private String nickname;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 게시글 연관관계 (양방향)
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Post> posts = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // 생성 메서드
    public static User create(String username, String password, String email, String nickname) {
        User user = new User();
        user.username = username;
        user.password = password;
        user.email = email;
        user.nickname = nickname;
        return user;
    }

    // 닉네임 수정
    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }

    // 비밀번호 수정
    public void updatePassword(String password) {
        this.password = password;
    }
}
