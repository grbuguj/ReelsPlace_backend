package com.example.reelsplace.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 릴스-장소 매핑 엔티티
 * ERD: ReelPlace 테이블
 * 릴스 1개에서 여러 장소가 나올 수 있는 N:M 관계 처리
 */
@Entity
@Table(name = "reel_places")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReelPlace {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reel_id", nullable = false)
    private Reel reel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_id", nullable = false)
    private Place place;

    @Builder
    public ReelPlace(Reel reel, Place place) {
        this.reel = reel;
        this.place = place;
    }
}
