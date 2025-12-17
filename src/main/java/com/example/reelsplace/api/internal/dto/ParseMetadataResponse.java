package com.example.reelsplace.api.internal.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 릴스 메타데이터 파싱 응답
 */
@Getter
@Builder
public class ParseMetadataResponse {
    private Long reelId;
    private String thumbnailUrl;
    private String caption;
    private LocalDateTime parsedAt;
}
