package com.example.reelsplace.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 장소 이미지 엔티티
 * ERD: PlaceImage 테이블
 */
@Entity
@Table(name = "place_images")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PlaceImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_id", nullable = false)
    private Place place;

    @Column(name = "image_url", nullable = false, length = 500)
    private String imageUrl;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    @Builder
    public PlaceImage(Place place, String imageUrl, Integer sortOrder) {
        this.place = place;
        this.imageUrl = imageUrl;
        this.sortOrder = sortOrder;
    }

    // 연관관계 편의 메서드
    void setPlace(Place place) {
        this.place = place;
    }
}
