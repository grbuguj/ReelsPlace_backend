package com.example.reelsplace.api.auth.controller;

import com.example.reelsplace.api.auth.dto.UpdateMapAppRequest;
import com.example.reelsplace.api.auth.dto.UserInfoResponse;
import com.example.reelsplace.api.auth.service.AuthService;
import com.example.reelsplace.global.annotation.AuthUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 인증 API 컨트롤러
 * Base: /api/v1/auth 및 /api/v1/users
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 내 정보 조회
     * GET /api/v1/users/me
     */
    @GetMapping("/users/me")
    public ResponseEntity<UserInfoResponse> getMyInfo(@AuthUser Long userId) {
        UserInfoResponse response = authService.getMyInfo(userId);
        return ResponseEntity.ok(response);
    }

    /**
     * 기본 지도 앱 설정 변경
     * PATCH /api/v1/users/me/map-app
     */
    @PatchMapping("/users/me/map-app")
    public ResponseEntity<UserInfoResponse> updateDefaultMapApp(
            @AuthUser Long userId,
            @Valid @RequestBody UpdateMapAppRequest request
    ) {
        UserInfoResponse response = authService.updateDefaultMapApp(userId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * 로그아웃
     * POST /api/v1/auth/logout
     * 
     * JWT는 Stateless이므로 서버에서 할 작업 없음
     * 클라이언트에서 토큰 삭제만 하면 됨
     */
    @PostMapping("/auth/logout")
    public ResponseEntity<Void> logout(@AuthUser Long userId) {
        // 로그 만 남기고 실제 처리 없음
        // 향후 Refresh Token을 DB에 저장한다면 여기서 삭제 필요
        return ResponseEntity.ok().build();
    }
}
