package com.example.reelsplace.api.reel.controller;

import com.example.reelsplace.api.reel.dto.ReelResponse;
import com.example.reelsplace.api.reel.dto.ReelSaveRequest;
import com.example.reelsplace.api.reel.service.ReelService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 릴스 API 컨트롤러
 * Base: /api/v1/reels
 */
@RestController
@RequestMapping("/api/v1/reels")
@RequiredArgsConstructor
public class ReelController {

    private final ReelService reelService;

    /**
     * 릴스 저장
     * POST /api/v1/reels
     */
    @PostMapping
    public ResponseEntity<ReelResponse> saveReel(
            // TODO: 인증 구현 후 @AuthUser Long userId로 변경
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody ReelSaveRequest request
    ) {
        ReelResponse response = reelService.saveReel(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 내 릴스 목록 조회
     * GET /api/v1/reels?page=0&size=20
     */
    @GetMapping
    public ResponseEntity<Page<ReelResponse>> getMyReels(
            @RequestHeader("X-User-Id") Long userId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<ReelResponse> response = reelService.getMyReels(userId, pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * 릴스 삭제
     * DELETE /api/v1/reels/{reelId}
     */
    @DeleteMapping("/{reelId}")
    public ResponseEntity<Void> deleteReel(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long reelId
    ) {
        reelService.deleteReel(userId, reelId);
        return ResponseEntity.noContent().build();
    }
}
