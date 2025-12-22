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

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * Google Places API 연동 서비스
 *
 * 역할:
 * - 매장명 / 주소 기반 장소 검색
 * - Text Search 쿼리 전략(fallback) 적용
 * - Place 엔티티 생성
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GooglePlacesService {

    private final GooglePlacesProperties properties;
    private final WebClient.Builder webClientBuilder;

    /**
     * 장소 검색 (매장명 + 주소 기반)
     *
     * @param user      장소 소유 사용자
     * @param placeName 매장명 (nullable)
     * @param address   주소 (nullable)
     * @return Place 엔티티 (검색 실패 시 null)
     */
    public Place searchPlace(User user, String placeName, String address) {
        WebClient webClient = webClientBuilder
                .baseUrl(properties.getBaseUrl())
                .build();

        List<String> queries = buildQueries(placeName, address);

        log.info("[GooglePlaces] 검색 시작 - placeName={}, address={}, queries={}",
                placeName, address, queries);

        if (queries.isEmpty()) {
            log.warn("[GooglePlaces] 쿼리 생성 실패 (placeName, address 모두 null)");
            return null;
        }

        for (String query : queries) {
            try {
                log.info("[GooglePlaces] 검색 시도 query='{}'", query);

                GooglePlacesResponse response = webClient.get()
                        .uri(uriBuilder -> {
                            URI uri = uriBuilder
                                    .path("/textsearch/json")
                                    .queryParam("query", query)
                                    .queryParam("key", properties.getApiKey())
                                    .queryParam("language", "ko")
                                    .build();
                            log.info("[GooglePlaces] 요청 URL = {}", uri);
                            return uri;
                        })
                        .retrieve()
                        .bodyToMono(GooglePlacesResponse.class)
                        .block();

                if (response == null) {
                    log.warn("[GooglePlaces] 응답 null");
                    continue;
                }

                log.info("[GooglePlaces] 응답 status={}, resultsCount={}",
                        response.getStatus(),
                        response.getResults() == null ? 0 : response.getResults().size());

                if (!"OK".equals(response.getStatus())) {
                    log.warn("[GooglePlaces] status != OK → {}", response.getStatus());
                    continue;
                }

                if (response.getResults() == null || response.getResults().isEmpty()) {
                    log.warn("[GooglePlaces] 결과 비어있음");
                    continue;
                }

                GooglePlacesResponse.PlaceResult r = response.getResults().get(0);

                log.info("[GooglePlaces] 1순위 결과 name='{}', address='{}', placeId={}",
                        r.getName(),
                        r.getFormattedAddress(),
                        r.getPlaceId());

                Place place = buildPlaceEntity(user, r);

                log.info("[GooglePlaces] 장소 생성 성공 name={}, googlePlaceId={}",
                        place.getName(),
                        place.getGooglePlaceId());

                return place;

            } catch (Exception e) {
                log.error("[GooglePlaces] API 호출 예외 query={}, error={}",
                        query, e.getMessage(), e);
            }
        }

        log.error("[GooglePlaces] 모든 쿼리 실패 - placeName={}, address={}",
                placeName, address);
        return null;
    }


    /**
     * 검색 쿼리 전략 생성
     */
    private List<String> buildQueries(String placeName, String address) {
        List<String> queries = new ArrayList<>();

        if (placeName != null && !placeName.isBlank()
                && address != null && !address.isBlank()) {
            // 1순위: 매장명 + 주소
            queries.add(placeName + " " + address);
        }

        if (placeName != null && !placeName.isBlank()) {
            // 2순위: 매장명만
            queries.add(placeName);
        }

        if (address != null && !address.isBlank()) {
            // 3순위: 주소 + 키워드 fallback
            queries.add(address + " 카페");
            queries.add(address + " 식당");
        }

        return queries;
    }

    /**
     * Google Places 결과 → Place 엔티티 변환
     */
    private Place buildPlaceEntity(User user, GooglePlacesResponse.PlaceResult placeResult) {
        Place place = Place.builder()
                .user(user)
                .googlePlaceId(placeResult.getPlaceId())
                .name(placeResult.getName())
                .address(placeResult.getFormattedAddress())
                .rating(placeResult.getRating())
                .reviewCount(placeResult.getUserRatingsTotal())
                .build();

        // 이미지 최대 3장 추가
        if (placeResult.getPhotos() != null && !placeResult.getPhotos().isEmpty()) {
            int photoCount = Math.min(3, placeResult.getPhotos().size());
            for (int i = 0; i < photoCount; i++) {
                String photoReference = placeResult.getPhotos().get(i).getPhotoReference();
                place.addImage(
                        PlaceImage.builder()
                                .place(place)
                                .imageUrl(buildPhotoUrl(photoReference))
                                .sortOrder(i) // 0 = 대표 이미지
                                .build()
                );
            }
        }

        return place;
    }

    /**
     * Photo Reference → 이미지 URL 생성
     */
    private String buildPhotoUrl(String photoReference) {
        return String.format(
                "%s/photo?maxwidth=400&photo_reference=%s&key=%s",
                properties.getBaseUrl(),
                photoReference,
                properties.getApiKey()
        );
    }
}
