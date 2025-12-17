package com.example.reelsplace.api.internal.service;

import com.example.reelsplace.api.internal.dto.SendNotificationRequest;
import com.example.reelsplace.api.internal.dto.SendNotificationResponse;
import com.example.reelsplace.domain.entity.User;
import com.example.reelsplace.domain.repository.UserRepository;
import com.example.reelsplace.global.exception.CustomException;
import com.example.reelsplace.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 푸시 알림 서비스
 * TODO: FCM 연동 필요
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final UserRepository userRepository;

    /**
     * 장소 생성 완료 알림 전송
     * POST /api/v1/internal/notifications/place-created
     */
    public SendNotificationResponse sendPlaceCreatedNotification(SendNotificationRequest request) {
        // 사용자 존재 확인
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        try {
            // TODO: FCM으로 실제 푸시 알림 전송
            // 현재는 로그만 남김
            String message = buildNotificationMessage(request.getPlaceCount());
            
            log.info("푸시 알림 전송 - userId: {}, reelId: {}, message: {}", 
                    request.getUserId(), request.getReelId(), message);

            // TODO: FCM 연동 후 실제 전송 로직 구현
            // FirebaseMessaging.getInstance().send(fcmMessage);

            return SendNotificationResponse.builder()
                    .userId(request.getUserId())
                    .reelId(request.getReelId())
                    .success(true)
                    .message(message)
                    .sentAt(LocalDateTime.now())
                    .build();

        } catch (Exception e) {
            log.error("푸시 알림 전송 실패 - userId: {}, reelId: {}, Error: {}", 
                    request.getUserId(), request.getReelId(), e.getMessage());

            return SendNotificationResponse.builder()
                    .userId(request.getUserId())
                    .reelId(request.getReelId())
                    .success(false)
                    .message("푸시 알림 전송 실패")
                    .sentAt(LocalDateTime.now())
                    .build();
        }
    }

    /**
     * 알림 메시지 생성
     */
    private String buildNotificationMessage(int placeCount) {
        if (placeCount == 1) {
            return "릴스에서 장소 1곳을 찾았어요! 🎉";
        } else {
            return String.format("릴스에서 장소 %d곳을 찾았어요! 🎉", placeCount);
        }
    }

    /**
     * FCM 연동 예시 (주석)
     * 
     * private void sendFcmNotification(User user, String message) {
     *     if (user.getFcmToken() == null) {
     *         log.warn("FCM 토큰이 없음 - userId: {}", user.getId());
     *         return;
     *     }
     * 
     *     Message fcmMessage = Message.builder()
     *             .setToken(user.getFcmToken())
     *             .setNotification(Notification.builder()
     *                     .setTitle("릴스플레이스")
     *                     .setBody(message)
     *                     .build())
     *             .build();
     * 
     *     FirebaseMessaging.getInstance().send(fcmMessage);
     * }
     */
}
