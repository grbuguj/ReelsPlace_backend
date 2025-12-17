package com.example.reelsplace.api.internal.dto;

import com.example.reelsplace.domain.enums.ReelStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 릴스 상태 변경 요청
 */
@Getter
@NoArgsConstructor
public class UpdateReelStatusRequest {
    
    @NotNull(message = "상태는 필수입니다.")
    private ReelStatus status;
}
