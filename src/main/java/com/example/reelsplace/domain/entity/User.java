package com.example.reelsplace.domain.entity;

import com.example.reelsplace.domain.enums.MapApp;
import com.example.reelsplace.domain.enums.Provider;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * 유저 엔티티
 * ERD: User 테이블
 */
@Entity
@Table(name = "users", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"provider", "provider_user_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Provider provider;

    @Column(name = "provider_user_id", nullable = false, length = 100)
    private String providerUserId;

    @Column(length = 255)
    private String email;

    @Column(length = 50)
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(name = "default_map_app", nullable = false, length = 20)
    private MapApp defaultMapApp;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public User(Provider provider, String providerUserId, String email, 
                String nickname, MapApp defaultMapApp) {
        this.provider = provider;
        this.providerUserId = providerUserId;
        this.email = email;
        this.nickname = nickname;
        this.defaultMapApp = defaultMapApp;
    }

    // 비즈니스 메서드
    public void updateDefaultMapApp(MapApp mapApp) {
        this.defaultMapApp = mapApp;
    }
}
