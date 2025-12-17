package com.example.reelsplace.api.place.service;

import com.example.reelsplace.api.place.dto.PlaceResponse;
import com.example.reelsplace.domain.entity.Place;
import com.example.reelsplace.domain.entity.User;
import com.example.reelsplace.domain.entity.UserStats;
import com.example.reelsplace.domain.repository.PlaceRepository;
import com.example.reelsplace.domain.repository.UserRepository;
import com.example.reelsplace.domain.repository.UserStatsRepository;
import com.example.reelsplace.global.exception.CustomException;
import com.example.reelsplace.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 장소 비즈니스 로직
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlaceService {

    private final PlaceRepository placeRepository;
    private final UserRepository userRepository;
    private final UserStatsRepository userStatsRepository;

    /**
     * 내 장소 목록 조회
     * API: GET /api/v1/places
     */
    public Page<PlaceResponse> getMyPlaces(Long userId, Pageable pageable) {
        // 사용자 존재 확인
        if (!userRepository.existsById(userId)) {
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }

        Page<Place> places = placeRepository.findByUserIdWithImagesOrderByCreatedAtDesc(userId, pageable);
        return places.map(PlaceResponse::from);
    }

    /**
     * 장소 삭제
     * API: DELETE /api/v1/places/{placeId}
     */
    @Transactional
    public void deletePlace(Long userId, Long placeId) {
        Place place = placeRepository.findByIdAndUserId(placeId, userId)
                .orElseThrow(() -> new CustomException(ErrorCode.PLACE_NOT_FOUND));

        placeRepository.delete(place);
        log.info("장소 삭제 완료 - placeId: {}", placeId);
    }

    /**
     * 지도 앱 열기 기록
     * API: POST /api/v1/places/{placeId}/open-map
     */
    @Transactional
    public void recordMapOpen(Long userId, Long placeId) {
        // 장소 존재 및 권한 확인
        Place place = placeRepository.findByIdAndUserId(placeId, userId)
                .orElseThrow(() -> new CustomException(ErrorCode.PLACE_NOT_FOUND));

        // UserStats 조회 또는 생성
        UserStats userStats = userStatsRepository.findByUserId(userId)
                .orElseGet(() -> {
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
                    return userStatsRepository.save(UserStats.builder()
                            .user(user)
                            .build());
                });

        // 카운트 증가
        userStats.incrementMapOpenCount();
        log.info("지도 앱 열기 기록 - userId: {}, placeId: {}, count: {}", 
                userId, placeId, userStats.getMapOpenCount());
    }
}
