package com.example.reelsplace.api.internal.controller;

import com.example.reelsplace.api.internal.dto.*;
import com.example.reelsplace.api.internal.service.InternalReelService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Internal API - 릴스 처리 컨트롤러
 * Base: /api/v1/internal/reels
 */
@RestController
@RequestMapping("/api/v1/internal/reels")
@RequiredArgsConstructor
public class InternalReelController {

    private final InternalReelService internalReelService;

    /**
     * 릴스 메타데이터 파싱
     * POST /api/v1/internal/reels/{reelId}/parse-metadata
     */
    @PostMapping("/{reelId}/parse-metadata")
    public ResponseEntity<ParseMetadataResponse> parseMetadata(@PathVariable Long reelId) {
        ParseMetadataResponse response = internalReelService.parseMetadata(reelId);
        return ResponseEntity.ok(response);
    }

    /**
     * 주소 추출
     * POST /api/v1/internal/reels/{reelId}/extract-addresses
     */
    @PostMapping("/{reelId}/extract-addresses")
    public ResponseEntity<ExtractAddressResponse> extractAddresses(@PathVariable Long reelId) {
        ExtractAddressResponse response = internalReelService.extractAddresses(reelId);
        return ResponseEntity.ok(response);
    }

    /**
     * 장소 생성
     * POST /api/v1/internal/reels/{reelId}/create-places
     */
    @PostMapping("/{reelId}/create-places")
    public ResponseEntity<CreatePlacesResponse> createPlaces(
            @PathVariable Long reelId,
            @Valid @RequestBody CreatePlacesRequest request
    ) {
        CreatePlacesResponse response = internalReelService.createPlaces(reelId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * 릴스 상태 변경 (디버깅용)
     * PATCH /api/v1/internal/reels/{reelId}/status
     */
    @PatchMapping("/{reelId}/status")
    public ResponseEntity<Void> updateReelStatus(
            @PathVariable Long reelId,
            @Valid @RequestBody UpdateReelStatusRequest request
    ) {
        internalReelService.updateReelStatus(reelId, request);
        return ResponseEntity.ok().build();
    }
}
