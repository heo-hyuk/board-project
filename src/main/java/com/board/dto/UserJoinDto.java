package com.board.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserJoinDto {

    @NotBlank(message = "아이디를 입력해주세요")
    @Size(min = 4, max = 20, message = "아이디는 4~20자여야 합니다")
    private String username;

    @NotBlank(message = "비밀번호를 입력해주세요")
    @Size(min = 6, message = "비밀번호는 6자 이상이어야 합니다")
    private String password;

    @NotBlank(message = "이메일을 입력해주세요")
    @Email(message = "이메일 형식이 올바르지 않습니다")
    private String email;

    @NotBlank(message = "닉네임을 입력해주세요")
    @Size(max = 20, message = "닉네임은 20자 이하여야 합니다")
    private String nickname;
}
