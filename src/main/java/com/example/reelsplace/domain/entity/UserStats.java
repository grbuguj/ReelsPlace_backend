package com.example.reelsplace.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 유저 통계 엔티티
 * ERD: UserStats 테이블
 * User 1:1 관계
 */
@Entity
@Table(name = "user_stats")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserStats {

    @Id
    private Long userId;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "map_open_count", nullable = false)
    private Integer mapOpenCount = 0;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    public UserStats(User user) {
        this.user = user;
        this.mapOpenCount = 0;
    }

    // 비즈니스 메서드
    public void incrementMapOpenCount() {
        this.mapOpenCount++;
    }
}
