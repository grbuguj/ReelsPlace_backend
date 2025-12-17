package com.example.reelsplace.api.internal.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 주소 추출 응답
 */
@Getter
@Builder
public class ExtractAddressResponse {
    private Long reelId;
    private List<String> addresses;
    private LocalDateTime extractedAt;
}
