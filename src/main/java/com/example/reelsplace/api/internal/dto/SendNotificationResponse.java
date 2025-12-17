package com.example.reelsplace.api.internal.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 푸시 알림 전송 응답
 */
@Getter
@Builder
public class SendNotificationResponse {
    private Long userId;
    private Long reelId;
    private boolean success;
    private String message;
    private LocalDateTime sentAt;
}
