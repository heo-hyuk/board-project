package com.board.api.comment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 댓글 작성 요청 DTO
@Getter
@NoArgsConstructor
public class CommentCreateRequest {

    @NotNull(message = "게시글 ID가 필요합니다.")
    private Long postId;

    @NotBlank(message = "댓글 내용을 입력해주세요.")
    @Size(max = 500, message = "댓글은 500자 이내로 입력해주세요.")
    private String content;
}
