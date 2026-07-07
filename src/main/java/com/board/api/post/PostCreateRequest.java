package com.board.api.post;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 게시글 작성 요청 DTO
@Getter
@NoArgsConstructor
public class PostCreateRequest {

    @NotBlank(message = "제목을 입력해주세요.")
    @Size(max = 200, message = "제목은 200자 이내로 입력해주세요.")
    private String title;

    @Size(max = 300, message = "요약은 300자 이내로 입력해주세요.")
    private String summary;

    @NotBlank(message = "내용을 입력해주세요.")
    private String content;

    private String category = "Java";

    // 쉼표 구분 태그 문자열 (예: "Spring,JPA,Redis")
    private String tags;
}
