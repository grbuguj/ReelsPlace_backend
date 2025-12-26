package com.example.reelsplace.api.internal.service;

import com.example.reelsplace.api.internal.dto.*;
import com.example.reelsplace.domain.entity.Place;
import com.example.reelsplace.domain.entity.Reel;
import com.example.reelsplace.domain.entity.ReelPlace;
import com.example.reelsplace.domain.enums.ReelStatus;
import com.example.reelsplace.domain.repository.PlaceRepository;
import com.example.reelsplace.domain.repository.ReelPlaceRepository;
import com.example.reelsplace.domain.repository.ReelRepository;
import com.example.reelsplace.global.exception.CustomException;
import com.example.reelsplace.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Internal API - 릴스 처리 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InternalReelService {

    private final ReelRepository reelRepository;
    private final PlaceRepository placeRepository;
    private final ReelPlaceRepository reelPlaceRepository;
    private final InstagramParsingService instagramParsingService;
    private final AddressExtractionService addressExtractionService;
    private final GooglePlacesService googlePlacesService;

    /**
     * 릴스 메타데이터 파싱
     * POST /api/v1/internal/reels/{reelId}/parse-metadata
     */
    @Transactional
    public ParseMetadataResponse parseMetadata(Long reelId) {
        Reel reel = reelRepository.findById(reelId)
                .orElseThrow(() -> new CustomException(ErrorCode.REEL_NOT_FOUND));

        try {
            // Instagram 파싱
            String[] metadata = instagramParsingService.parseReelMetadata(reel.getReelUrl());
            String thumbnailUrl = metadata[0];
            String caption = metadata[1];

            // Reel 업데이트
            reel.updateMetadata(thumbnailUrl, caption);

            log.info("릴스 메타데이터 파싱 완료 - reelId: {}", reelId);

            return ParseMetadataResponse.builder()
                    .reelId(reelId)
                    .thumbnailUrl(thumbnailUrl)
                    .caption(caption)
                    .parsedAt(LocalDateTime.now())
                    .build();

        } catch (Exception e) {
            // 파싱 실패 시 상태 변경
            reel.updateStatus(ReelStatus.FAILED);
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 주소 추출
     * POST /api/v1/internal/reels/{reelId}/extract-addresses
     */
    public ExtractAddressResponse extractAddresses(Long reelId) {
        Reel reel = reelRepository.findById(reelId)
                .orElseThrow(() -> new CustomException(ErrorCode.REEL_NOT_FOUND));

        if (reel.getCaption() == null || reel.getCaption().isBlank()) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }

        // 캡션에서 주소 추출
        List<String> addresses = addressExtractionService.extractAddresses(reel.getCaption());

        log.info("주소 추출 완료 - reelId: {}, 주소 개수: {}", reelId, addresses.size());

        return ExtractAddressResponse.builder()
                .reelId(reelId)
                .addresses(addresses)
                .extractedAt(LocalDateTime.now())
                .build();
    }

    /**
     * 장소 생성
     * POST /api/v1/internal/reels/{reelId}/create-places
     */
    @Transactional
    public CreatePlacesResponse createPlaces(Long reelId, CreatePlacesRequest request) {
        Reel reel = reelRepository.findById(reelId)
                .orElseThrow(() -> new CustomException(ErrorCode.REEL_NOT_FOUND));

        List<CreatePlacesResponse.CreatedPlace> createdPlaces = new ArrayList<>();
        List<String> failedTargets = new ArrayList<>();

        // 🔑 캡션에서 매장명 추출 (없으면 null)
        String placeName = addressExtractionService
                .extractPlaceName(reel.getCaption())
                .orElseGet(() ->
                        request.getAddresses().isEmpty()
                                ? null
                                : addressExtractionService
                                .extractPlaceNameNearAddress(
                                        reel.getCaption(),
                                        request.getAddresses().get(0)
                                )
                                .orElse(null)
                );


        // 각 주소로 Google Places 검색
        for (String address : request.getAddresses()) {
            try {
                Place place = googlePlacesService.searchPlace(
                        reel.getUser(),
                        placeName,
                        address
                );

                if (place == null) {
                    continue;
                }

                // 🔁 중복 장소 체크
                Place savedPlace;
                if (placeRepository.existsByUserIdAndGooglePlaceId(
                        reel.getUser().getId(),
                        place.getGooglePlaceId())) {

                    savedPlace = placeRepository
                            .findByUserIdAndGooglePlaceId(
                                    reel.getUser().getId(),
                                    place.getGooglePlaceId())
                            .orElseThrow(); // 논리상 존재 보장

                } else {
                    savedPlace = placeRepository.save(place);
                }

                // Reel ↔ Place 매핑
                ReelPlace reelPlace = ReelPlace.builder()
                        .reel(reel)
                        .place(savedPlace)
                        .build();
                reelPlaceRepository.save(reelPlace);

                // 응답 DTO
                createdPlaces.add(
                        CreatePlacesResponse.CreatedPlace.builder()
                                .placeId(savedPlace.getId())
                                .googlePlaceId(savedPlace.getGooglePlaceId())
                                .name(savedPlace.getName())
                                .address(savedPlace.getAddress())
                                .imageCount(savedPlace.getImages().size())
                                .build()
                );

            } catch (Exception e) {
                log.error(
                        "장소 생성 실패 - reelId: {}, placeName: {}, address: {}, error: {}",
                        reelId, placeName, address, e.getMessage()
                );
            }
        }



        // 🎯 릴스 상태 업데이트
        updateReelStatus(reel, request.getAddresses(), createdPlaces);

        log.info(
                "장소 생성 완료 - reelId: {}, 성공: {}, 실패: {}",
                reelId, createdPlaces.size(), failedTargets.size()
        );

        return CreatePlacesResponse.builder()
                .reelId(reelId)
                .createdPlaces(createdPlaces)
                .failedAddresses(failedTargets)
                .createdAt(LocalDateTime.now())
                .build();
    }

    private void updateReelStatus(
            Reel reel,
            List<String> addresses,
            List<CreatePlacesResponse.CreatedPlace> createdPlaces
    ) {
        if (!createdPlaces.isEmpty()) {
            reel.updateStatus(ReelStatus.PLACE_FOUND);
        } else if (addresses.isEmpty()) {
            reel.updateStatus(ReelStatus.NO_ADDRESS);
        } else {
            reel.updateStatus(ReelStatus.PLACE_NOT_FOUND);
        }
    }


    /**
     * 릴스 상태 변경 (디버깅용)
     * PATCH /api/v1/internal/reels/{reelId}/status
     */
    @Transactional
    public void updateReelStatus(Long reelId, UpdateReelStatusRequest request) {
        Reel reel = reelRepository.findById(reelId)
                .orElseThrow(() -> new CustomException(ErrorCode.REEL_NOT_FOUND));

        ReelStatus oldStatus = reel.getStatus();
        reel.updateStatus(request.getStatus());

        log.info("릴스 상태 변경 - reelId: {}, {} → {}", reelId, oldStatus, request.getStatus());
    }
}
