package com.example.reelsplace.domain.entity;

import com.example.reelsplace.domain.enums.ReelStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 릴스 엔티티
 * ERD: Reel 테이블
 */
@Entity
@Table(name = "reels",
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "reel_url"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Reel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "reel", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReelPlace> reelPlaces = new ArrayList<>();

    @Column(name = "reel_url", nullable = false, length = 500)
    private String reelUrl;

    @Column(name = "thumbnail_url", length = 500)
    private String thumbnailUrl;

    @Column(columnDefinition = "TEXT")
    private String caption;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReelStatus status;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public Reel(User user, String reelUrl) {
        this.user = user;
        this.reelUrl = reelUrl;
        this.status = ReelStatus.PROCESSING;
    }

    // 비즈니스 메서드
    public void updateMetadata(String thumbnailUrl, String caption) {
        this.thumbnailUrl = thumbnailUrl;
        this.caption = caption;
    }

    public void updateStatus(ReelStatus status) {
        this.status = status;
    }
}
