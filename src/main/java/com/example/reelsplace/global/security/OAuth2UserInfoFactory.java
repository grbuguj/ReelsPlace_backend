package com.example.reelsplace.global.security;

import com.example.reelsplace.domain.enums.Provider;

import java.util.Map;

/**
 * OAuth2 사용자 정보 팩토리
 */
public class OAuth2UserInfoFactory {
    
    public static OAuth2UserInfo getOAuth2UserInfo(String registrationId, Map<String, Object> attributes) {
        Provider provider = Provider.valueOf(registrationId.toUpperCase());
        
        return switch (provider) {
            case GOOGLE -> new GoogleOAuth2UserInfo(attributes);
            case KAKAO -> new KakaoOAuth2UserInfo(attributes);
            case NAVER -> new NaverOAuth2UserInfo(attributes);
        };
    }
}
