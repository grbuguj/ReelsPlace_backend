package com.example.reelsplace.api.internal.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 장소 생성 응답
 */
@Getter
@Builder
public class CreatePlacesResponse {
    private Long reelId;
    private List<CreatedPlace> createdPlaces;
    private List<String> failedAddresses;
    private LocalDateTime createdAt;
    
    @Getter
    @Builder
    public static class CreatedPlace {
        private Long placeId;
        private String googlePlaceId;
        private String name;
        private String address;
        private Integer imageCount;
    }
}
