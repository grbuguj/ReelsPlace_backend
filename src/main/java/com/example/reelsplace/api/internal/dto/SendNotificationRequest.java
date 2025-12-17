package com.example.reelsplace.api.internal.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 푸시 알림 전송 요청
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendNotificationRequest {
    
    @NotNull(message = "사용자 ID는 필수입니다.")
    private Long userId;
    
    @NotNull(message = "릴스 ID는 필수입니다.")
    private Long reelId;
    
    @NotNull(message = "생성된 장소 개수는 필수입니다.")
    private Integer placeCount;
}
