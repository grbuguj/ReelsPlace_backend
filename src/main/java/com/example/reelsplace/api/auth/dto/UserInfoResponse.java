package com.example.reelsplace.api.auth.dto;

import com.example.reelsplace.domain.entity.User;
import com.example.reelsplace.domain.enums.MapApp;
import com.example.reelsplace.domain.enums.Provider;
import lombok.Builder;
import lombok.Getter;

/**
 * 사용자 정보 응답 DTO
 */
@Getter
@Builder
public class UserInfoResponse {
    private Long id;
    private Provider provider;
    private String email;
    private String nickname;
    private MapApp defaultMapApp;
    
    public static UserInfoResponse from(User user) {
        return UserInfoResponse.builder()
                .id(user.getId())
                .provider(user.getProvider())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .defaultMapApp(user.getDefaultMapApp())
                .build();
    }
}
