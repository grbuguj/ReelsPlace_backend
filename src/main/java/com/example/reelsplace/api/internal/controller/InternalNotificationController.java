package com.example.reelsplace.api.internal.controller;

import com.example.reelsplace.api.internal.dto.SendNotificationRequest;
import com.example.reelsplace.api.internal.dto.SendNotificationResponse;
import com.example.reelsplace.api.internal.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Internal API - 알림 컨트롤러
 * Base: /api/v1/internal/notifications
 */
@RestController
@RequestMapping("/api/v1/internal/notifications")
@RequiredArgsConstructor
public class InternalNotificationController {

    private final NotificationService notificationService;

    /**
     * 장소 생성 완료 알림 전송
     * POST /api/v1/internal/notifications/place-created
     */
    @PostMapping("/place-created")
    public ResponseEntity<SendNotificationResponse> sendPlaceCreatedNotification(
            @Valid @RequestBody SendNotificationRequest request
    ) {
        SendNotificationResponse response = notificationService.sendPlaceCreatedNotification(request);
        return ResponseEntity.ok(response);
    }
}
