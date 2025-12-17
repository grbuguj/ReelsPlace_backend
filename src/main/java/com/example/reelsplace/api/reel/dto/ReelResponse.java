package com.example.reelsplace.api.reel.dto;

import com.example.reelsplace.domain.entity.Reel;
import com.example.reelsplace.domain.enums.ReelStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 릴스 응답 DTO
 */
@Getter
@Builder
public class ReelResponse {
    
    private Long id;
    private String reelUrl;
    private String thumbnailUrl;
    private String caption;
    private ReelStatus status;
    private LocalDateTime createdAt;
    
    public static ReelResponse from(Reel reel) {
        return ReelResponse.builder()
                .id(reel.getId())
                .reelUrl(reel.getReelUrl())
                .thumbnailUrl(reel.getThumbnailUrl())
                .caption(reel.getCaption())
                .status(reel.getStatus())
                .createdAt(reel.getCreatedAt())
                .build();
    }
}
