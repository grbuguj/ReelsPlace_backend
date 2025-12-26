package com.example.reelsplace.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 장소 엔티티
 * ERD: Place 테이블
 */
@Entity
@Table(name = "places",
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "google_place_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Place {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "google_place_id", nullable = false, length = 100)
    private String googlePlaceId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, length = 500)
    private String address;

    @OneToMany(mappedBy = "place", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReelPlace> reelPlaces = new ArrayList<>();

    @Column(precision = 2, scale = 1)
    private BigDecimal rating;

    @Column(name = "review_count")
    private Integer reviewCount;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "place", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PlaceImage> images = new ArrayList<>();

    @Builder
    public Place(User user, String googlePlaceId, String name, String address,
                 BigDecimal rating, Integer reviewCount) {
        this.user = user;
        this.googlePlaceId = googlePlaceId;
        this.name = name;
        this.address = address;
        this.rating = rating;
        this.reviewCount = reviewCount;
    }

    // 연관관계 편의 메서드
    public void addImage(PlaceImage image) {
        this.images.add(image);
        image.setPlace(this);
    }
}
