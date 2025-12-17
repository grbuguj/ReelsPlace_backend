package com.example.reelsplace.api.reel.service;

import com.example.reelsplace.api.reel.dto.ReelResponse;
import com.example.reelsplace.api.reel.dto.ReelSaveRequest;
import com.example.reelsplace.domain.entity.Reel;
import com.example.reelsplace.domain.entity.User;
import com.example.reelsplace.domain.repository.ReelRepository;
import com.example.reelsplace.domain.repository.UserRepository;
import com.example.reelsplace.global.exception.CustomException;
import com.example.reelsplace.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 릴스 비즈니스 로직
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReelService {

    private final ReelRepository reelRepository;
    private final UserRepository userRepository;

    /**
     * 릴스 저장
     * API: POST /api/v1/reels
     */
    @Transactional
    public ReelResponse saveReel(Long userId, ReelSaveRequest request) {
        // 1. 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 2. 중복 체크
        if (reelRepository.existsByUserIdAndReelUrl(userId, request.getReelUrl())) {
            throw new CustomException(ErrorCode.REEL_ALREADY_EXISTS);
        }

        // 3. 릴스 URL 검증 (기본적인 검증만)
        validateReelUrl(request.getReelUrl());

        // 4. 릴스 저장 (상태: PROCESSING)
        Reel reel = Reel.builder()
                .user(user)
                .reelUrl(request.getReelUrl())
                .build();

        Reel savedReel = reelRepository.save(reel);
        
        // TODO: 비동기로 파싱 작업 트리거 필요
        log.info("릴스 저장 완료 - reelId: {}, status: PROCESSING", savedReel.getId());

        return ReelResponse.from(savedReel);
    }

    /**
     * 내 릴스 목록 조회
     * API: GET /api/v1/reels
     */
    public Page<ReelResponse> getMyReels(Long userId, Pageable pageable) {
        Page<Reel> reels = reelRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        return reels.map(ReelResponse::from);
    }

    /**
     * 릴스 삭제
     * API: DELETE /api/v1/reels/{reelId}
     */
    @Transactional
    public void deleteReel(Long userId, Long reelId) {
        Reel reel = reelRepository.findByIdAndUserId(reelId, userId)
                .orElseThrow(() -> new CustomException(ErrorCode.REEL_NOT_FOUND));

        reelRepository.delete(reel);
        log.info("릴스 삭제 완료 - reelId: {}", reelId);
    }

    /**
     * 릴스 URL 기본 검증
     */
    private void validateReelUrl(String reelUrl) {
        if (!reelUrl.contains("instagram.com/reel/")) {
            throw new CustomException(ErrorCode.INVALID_REEL_URL);
        }
    }
}
