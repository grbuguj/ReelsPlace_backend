package com.example.reelsplace.api.place.dto;

import com.example.reelsplace.domain.entity.Place;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 장소 응답 DTO
 */
@Getter
@Builder
public class PlaceResponse {
    
    private Long id;
    private String googlePlaceId;
    private String name;
    private String address;
    private BigDecimal rating;
    private Integer reviewCount;
    private List<String> images;
    private LocalDateTime createdAt;
    
    public static PlaceResponse from(Place place) {
        List<String> imageUrls = place.getImages().stream()
                .map(img -> img.getImageUrl())
                .toList();
        
        return PlaceResponse.builder()
                .id(place.getId())
                .googlePlaceId(place.getGooglePlaceId())
                .name(place.getName())
                .address(place.getAddress())
                .rating(place.getRating())
                .reviewCount(place.getReviewCount())
                .images(imageUrls)
                .createdAt(place.getCreatedAt())
                .build();
    }
}
