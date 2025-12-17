package com.example.reelsplace.api.reel.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 릴스 저장 요청 DTO
 * API: POST /api/v1/reels
 */
@Getter
@NoArgsConstructor
public class ReelSaveRequest {
    
    @NotBlank(message = "릴스 URL은 필수입니다.")
    private String reelUrl;
}
