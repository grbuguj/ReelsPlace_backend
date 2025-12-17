package com.example.reelsplace.api.internal.service;

import com.example.reelsplace.api.internal.dto.GooglePlacesResponse;
import com.example.reelsplace.domain.entity.Place;
import com.example.reelsplace.domain.entity.PlaceImage;
import com.example.reelsplace.domain.entity.User;
import com.example.reelsplace.global.config.GooglePlacesProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;

/**
 * Google Places API 연동 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GooglePlacesService {

    private final GooglePlacesProperties properties;
    private final WebClient.Builder webClientBuilder;

    /**
     * 주소로 장소 검색
     * @return Place 엔티티 (1순위 결과만)
     */
    public Place searchPlace(User user, String address) {
        try {
            WebClient webClient = webClientBuilder.baseUrl(properties.getBaseUrl()).build();

            // Text Search API 호출
            GooglePlacesResponse response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/textsearch/json")
                            .queryParam("query", address)
                            .queryParam("key", properties.getApiKey())
                            .queryParam("language", "ko")
                            .build())
                    .retrieve()
                    .bodyToMono(GooglePlacesResponse.class)
                    .block();

            if (response == null || !"OK".equals(response.getStatus()) || response.getResults().isEmpty()) {
                log.warn("Google Places 검색 결과 없음 - 주소: {}", address);
                return null;
            }

            // 1순위 결과만 사용
            GooglePlacesResponse.PlaceResult placeResult = response.getResults().get(0);
            
            // Place 엔티티 생성
            Place place = Place.builder()
                    .user(user)
                    .googlePlaceId(placeResult.getPlaceId())
                    .name(placeResult.getName())
                    .address(placeResult.getFormattedAddress())
                    .rating(placeResult.getRating())
                    .reviewCount(placeResult.getUserRatingsTotal())
                    .build();

            // 이미지 추가 (최대 3장, 순서대로)
            if (placeResult.getPhotos() != null && !placeResult.getPhotos().isEmpty()) {
                int photoCount = Math.min(3, placeResult.getPhotos().size());
                for (int i = 0; i < photoCount; i++) {
                    String photoReference = placeResult.getPhotos().get(i).getPhotoReference();
                    String imageUrl = buildPhotoUrl(photoReference);
                    
                    PlaceImage placeImage = PlaceImage.builder()
                            .place(place)
                            .imageUrl(imageUrl)
                            .sortOrder(i) // 0 = 대표 이미지
                            .build();
                    
                    place.addImage(placeImage);
                }
            }

            log.info("Google Places 검색 성공 - 장소: {}, placeId: {}", place.getName(), place.getGooglePlaceId());
            return place;

        } catch (Exception e) {
            log.error("Google Places API 호출 실패 - 주소: {}, Error: {}", address, e.getMessage());
            return null;
        }
    }

    /**
     * Photo Reference로 이미지 URL 생성
     */
    private String buildPhotoUrl(String photoReference) {
        return String.format("%s/photo?maxwidth=400&photo_reference=%s&key=%s",
                properties.getBaseUrl(), photoReference, properties.getApiKey());
    }
}
