package com.example.reelsplace.api.reel.service;

import com.example.reelsplace.api.internal.dto.CreatePlacesRequest;
import com.example.reelsplace.api.internal.dto.CreatePlacesResponse;
import com.example.reelsplace.api.internal.dto.ExtractAddressResponse;
import com.example.reelsplace.api.internal.dto.SendNotificationRequest;
import com.example.reelsplace.api.internal.service.InternalReelService;
import com.example.reelsplace.api.internal.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 릴스 비동기 처리 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReelProcessingService {

    private final InternalReelService internalReelService;
    private final NotificationService notificationService;

    /**
     * 릴스 처리 파이프라인 (비동기)
     * 1. 메타데이터 파싱
     * 2. 주소 추출
     * 3. 장소 생성
     * 4. 푸시 알림 (장소가 생성된 경우만)
     */
    @Async
    public void processReelAsync(Long reelId, Long userId) {
        log.info("릴스 비동기 처리 시작 - reelId: {}", reelId);

        try {
            // 1. 메타데이터 파싱
            internalReelService.parseMetadata(reelId);
            log.info("1/4 메타데이터 파싱 완료 - reelId: {}", reelId);

            // 2. 주소 추출
            ExtractAddressResponse extractResponse = internalReelService.extractAddresses(reelId);
            List<String> addresses = extractResponse.getAddresses();
            log.info("2/4 주소 추출 완료 - reelId: {}, 주소 개수: {}", reelId, addresses.size());

            // 주소가 없으면 여기서 종료 (상태는 NO_ADDRESS로 이미 설정됨)
            if (addresses.isEmpty()) {
                log.info("주소 없음 - 처리 종료: reelId: {}", reelId);
                return;
            }

            // 3. 장소 생성
            CreatePlacesRequest createRequest = CreatePlacesRequest.builder()
                    .addresses(addresses)
                    .build();
            
            CreatePlacesResponse createResponse = internalReelService.createPlaces(reelId, createRequest);
            int placeCount = createResponse.getCreatedPlaces().size();
            log.info("3/4 장소 생성 완료 - reelId: {}, 장소 개수: {}", reelId, placeCount);

            // 4. 푸시 알림 (장소가 생성된 경우만)
            if (placeCount > 0) {
                SendNotificationRequest notificationRequest = SendNotificationRequest.builder()
                        .userId(userId)
                        .reelId(reelId)
                        .placeCount(placeCount)
                        .build();
                
                notificationService.sendPlaceCreatedNotification(notificationRequest);
                log.info("4/4 푸시 알림 전송 완료 - reelId: {}", reelId);
            } else {
                log.info("장소 생성 실패 - 푸시 알림 전송 안함: reelId: {}", reelId);
            }

            log.info("릴스 비동기 처리 완료 - reelId: {}", reelId);

        } catch (Exception e) {
            log.error("릴스 비동기 처리 실패 - reelId: {}, Error: {}", reelId, e.getMessage(), e);
        }
    }
}
