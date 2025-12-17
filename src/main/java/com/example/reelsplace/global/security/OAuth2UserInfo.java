package com.example.reelsplace.global.security;

import com.example.reelsplace.domain.enums.Provider;

import java.util.Map;

/**
 * OAuth2 사용자 정보 추상 클래스
 */
public abstract class OAuth2UserInfo {
    
    protected Map<String, Object> attributes;
    
    public OAuth2UserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
    }
    
    public abstract String getProviderId();
    public abstract Provider getProvider();
    public abstract String getEmail();
    public abstract String getNickname();
}
