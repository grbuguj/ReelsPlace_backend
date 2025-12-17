package com.example.reelsplace.api.user.service;

import com.example.reelsplace.api.user.dto.UserStatsResponse;
import com.example.reelsplace.domain.entity.User;
import com.example.reelsplace.domain.entity.UserStats;
import com.example.reelsplace.domain.repository.PlaceRepository;
import com.example.reelsplace.domain.repository.ReelRepository;
import com.example.reelsplace.domain.repository.UserRepository;
import com.example.reelsplace.domain.repository.UserStatsRepository;
import com.example.reelsplace.global.exception.CustomException;
import com.example.reelsplace.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 사용자 비즈니스 로직
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final UserStatsRepository userStatsRepository;
    private final ReelRepository reelRepository;
    private final PlaceRepository placeRepository;

    /**
     * 마이페이지 통계 조회
     * API: GET /api/v1/users/me/stats
     */
    public UserStatsResponse getMyStats(Long userId) {
        // 사용자 존재 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 통계 조회
        long reelCount = reelRepository.countByUserId(userId);
        long placeCount = placeRepository.countByUserId(userId);

        // UserStats 조회 (없으면 기본값)
        UserStats userStats = userStatsRepository.findByUserId(userId)
                .orElseGet(() -> UserStats.builder().user(user).build());

        return UserStatsResponse.builder()
                .userId(userId)
                .reelCount(reelCount)
                .placeCount(placeCount)
                .mapOpenCount(userStats.getMapOpenCount())
                .lastOpenedAt(userStats.getUpdatedAt())
                .build();
    }
}
