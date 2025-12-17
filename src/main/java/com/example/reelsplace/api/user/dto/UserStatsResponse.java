package com.example.reelsplace.api.user.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 사용자 통계 응답 DTO
 * API: GET /api/v1/users/me/stats
 */
@Getter
@Builder
public class UserStatsResponse {
    
    private Long userId;
    private Long reelCount;
    private Long placeCount;
    private Integer mapOpenCount;
    private LocalDateTime lastOpenedAt;
}
