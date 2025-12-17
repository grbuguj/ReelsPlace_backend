package com.example.reelsplace.api.auth.service;

import com.example.reelsplace.api.auth.dto.UpdateMapAppRequest;
import com.example.reelsplace.api.auth.dto.UserInfoResponse;
import com.example.reelsplace.domain.entity.User;
import com.example.reelsplace.domain.repository.UserRepository;
import com.example.reelsplace.global.exception.CustomException;
import com.example.reelsplace.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 인증 관련 비즈니스 로직
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;

    /**
     * 내 정보 조회
     */
    public UserInfoResponse getMyInfo(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        
        return UserInfoResponse.from(user);
    }

    /**
     * 기본 지도 앱 변경
     */
    @Transactional
    public UserInfoResponse updateDefaultMapApp(Long userId, UpdateMapAppRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        
        user.updateDefaultMapApp(request.getDefaultMapApp());
        
        log.info("기본 지도 앱 변경 - userId: {}, mapApp: {}", userId, request.getDefaultMapApp());
        
        return UserInfoResponse.from(user);
    }
}
