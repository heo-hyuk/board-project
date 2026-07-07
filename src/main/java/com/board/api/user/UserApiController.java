package com.board.api.user;

import com.board.api.common.ApiResponse;
import com.board.domain.User;
import com.board.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "User", description = "사용자 API")
public class UserApiController {

    private final UserService userService;

    // GET /api/v1/users/me
    @GetMapping("/me")
    @Operation(summary = "내 정보 조회", description = "로그인 필요 - password 제외한 회원 정보 반환")
    public ApiResponse<UserMeResponse> me(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByUsername(userDetails.getUsername());
        return ApiResponse.ok(UserMeResponse.from(user));
    }
}
