package com.board.dto;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
public class CommentDto {

    private Long id;
    private String content;
    private String nickname;   // 작성자 닉네임
    private Long userId;
    private String username;    // 댓글 작성자 아이디 (삭제 권한 확인용)
    private LocalDateTime createdAt;
}
