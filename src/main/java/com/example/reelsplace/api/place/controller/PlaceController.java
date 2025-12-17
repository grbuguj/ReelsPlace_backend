package com.example.reelsplace.api.place.controller;

import com.example.reelsplace.api.place.dto.PlaceResponse;
import com.example.reelsplace.api.place.service.PlaceService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 장소 API 컨트롤러
 * Base: /api/v1/places
 */
@RestController
@RequestMapping("/api/v1/places")
@RequiredArgsConstructor
public class PlaceController {

    private final PlaceService placeService;

    /**
     * 내 장소 목록 조회
     * GET /api/v1/places?page=0&size=20
     */
    @GetMapping
    public ResponseEntity<Page<PlaceResponse>> getMyPlaces(
            @RequestHeader("X-User-Id") Long userId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<PlaceResponse> response = placeService.getMyPlaces(userId, pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * 장소 삭제
     * DELETE /api/v1/places/{placeId}
     */
    @DeleteMapping("/{placeId}")
    public ResponseEntity<Void> deletePlace(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long placeId
    ) {
        placeService.deletePlace(userId, placeId);
        return ResponseEntity.noContent().build();
    }

    /**
     * 지도 앱 열기 기록
     * POST /api/v1/places/{placeId}/open-map
     */
    @PostMapping("/{placeId}/open-map")
    public ResponseEntity<Void> recordMapOpen(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long placeId
    ) {
        placeService.recordMapOpen(userId, placeId);
        return ResponseEntity.ok().build();
    }
}
