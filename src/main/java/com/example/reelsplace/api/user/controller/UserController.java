package com.example.reelsplace.api.user.controller;

import com.example.reelsplace.api.user.dto.UserStatsResponse;
import com.example.reelsplace.api.user.service.UserService;
import com.example.reelsplace.global.annotation.AuthUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 사용자 API 컨트롤러
 * Base: /api/v1/users
 */
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 마이페이지 통계 조회
     * GET /api/v1/users/me/stats
     */
    @GetMapping("/me/stats")
    public ResponseEntity<UserStatsResponse> getMyStats(@AuthUser Long userId) {
        UserStatsResponse response = userService.getMyStats(userId);
        return ResponseEntity.ok(response);
    }
}
