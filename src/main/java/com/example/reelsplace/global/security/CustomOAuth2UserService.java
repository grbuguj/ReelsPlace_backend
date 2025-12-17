package com.example.reelsplace.global.security;

import com.example.reelsplace.domain.entity.User;
import com.example.reelsplace.domain.entity.UserStats;
import com.example.reelsplace.domain.enums.MapApp;
import com.example.reelsplace.domain.repository.UserRepository;
import com.example.reelsplace.domain.repository.UserStatsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * OAuth2 사용자 정보 처리 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final UserStatsRepository userStatsRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        OAuth2UserInfo userInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(
                registrationId, 
                oAuth2User.getAttributes()
        );

        // 사용자 조회 또는 생성
        User user = userRepository.findByProviderAndProviderUserId(
                        userInfo.getProvider(),
                        userInfo.getProviderId()
                )
                .orElseGet(() -> createUser(userInfo));

        log.info("OAuth2 로그인 성공 - userId: {}, provider: {}", user.getId(), user.getProvider());

        return new CustomOAuth2User(user, oAuth2User.getAttributes());
    }

    /**
     * 신규 사용자 생성
     */
    private User createUser(OAuth2UserInfo userInfo) {
        // 기본 지도 앱은 네이버로 설정
        User user = User.builder()
                .provider(userInfo.getProvider())
                .providerUserId(userInfo.getProviderId())
                .email(userInfo.getEmail())
                .nickname(userInfo.getNickname())
                .defaultMapApp(MapApp.NAVER)
                .build();
        
        User savedUser = userRepository.save(user);
        
        // UserStats 생성
        UserStats userStats = UserStats.builder()
                .user(savedUser)
                .build();
        userStatsRepository.save(userStats);
        
        log.info("신규 사용자 생성 완료 - userId: {}", savedUser.getId());
        
        return savedUser;
    }
}
