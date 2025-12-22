package com.example.reelsplace.api.internal.service;

import com.example.reelsplace.api.internal.dto.CreatePlacesRequest;
import com.example.reelsplace.api.internal.dto.CreatePlacesResponse;
import com.example.reelsplace.api.internal.dto.ExtractAddressResponse;
import com.example.reelsplace.domain.entity.Reel;
import com.example.reelsplace.domain.entity.User;
import com.example.reelsplace.domain.enums.ReelStatus;
import com.example.reelsplace.domain.repository.ReelRepository;
import com.example.reelsplace.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 릴스 처리 통합 테스트
 * Mock 캡션 → 주소 추출 → 장소 생성 전체 플로우 테스트
 */
@SpringBootTest
@Transactional
class ReelProcessingIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ReelRepository reelRepository;

    @Autowired
    private InternalReelService internalReelService;

    @Autowired
    private AddressExtractionService addressExtractionService;

    private User testUser;
    private Reel testReel;

    @BeforeEach
    void setUp() {
        // 테스트 유저 생성
        testUser = User.builder()
                .email("test@example.com")
                .name("테스트유저")
                .profileImageUrl("https://example.com/profile.jpg")
                .build();
        testUser = userRepository.save(testUser);

        // Mock 캡션 데이터
        String mockCaption = """
                🍕 오늘의 맛집 탐방 🍕
                
                진짜 맛있는 피자 집 찾았어요!
                주소: 서울특별시 마포구 연남동 239-10
                
                📍 위치 정보
                - 지하철 2호선 홍대입구역 3번 출구
                - 도보 10분 거리
                
                💰 가격대: 1.5~2만원
                ⭐ 별점: 4.8/5.0
                
                #연남동맛집 #피자맛집 #데이트코스 
                #홍대맛집 #서울맛집 #먹스타그램
                """;

        // 테스트 릴스 생성 (캡션 포함)
        testReel = Reel.builder()
                .user(testUser)
                .reelUrl("https://www.instagram.com/reel/test123")
                .thumbnailUrl("https://example.com/thumbnail.jpg")
                .caption(mockCaption)
                .status(ReelStatus.METADATA_PARSED)
                .build();
        testReel = reelRepository.save(testReel);

        System.out.println("\n=== 테스트 셋업 완료 ===");
        System.out.println("User ID: " + testUser.getId());
        System.out.println("Reel ID: " + testReel.getId());
        System.out.println("Caption: \n" + mockCaption);
    }

    @Test
    @DisplayName("전체 플로우 테스트: Mock 캡션 → 주소 추출 → 장소 생성")
    void testFullFlow_mockCaptionToPlaceCreation() {
        // ===== STEP 1: 주소 추출 =====
        System.out.println("\n\n========== STEP 1: 주소 추출 ==========");
        
        ExtractAddressResponse extractResponse = internalReelService.extractAddresses(testReel.getId());
        
        System.out.println("추출된 주소 개수: " + extractResponse.getAddresses().size());
        extractResponse.getAddresses().forEach(addr -> 
            System.out.println("  - " + addr)
        );

        // 검증
        assertThat(extractResponse.getReelId()).isEqualTo(testReel.getId());
        assertThat(extractResponse.getAddresses()).isNotEmpty();
        assertThat(extractResponse.getAddresses())
                .anyMatch(addr -> addr.contains("마포구") || addr.contains("연남동"));

        // ===== STEP 2: 장소 생성 (Google Places API 호출) =====
        System.out.println("\n\n========== STEP 2: 장소 생성 ==========");
        
        CreatePlacesRequest createRequest = CreatePlacesRequest.builder()
                .addresses(extractResponse.getAddresses())
                .build();

        CreatePlacesResponse createResponse = internalReelService.createPlaces(
                testReel.getId(), 
                createRequest
        );

        System.out.println("생성된 장소 개수: " + createResponse.getCreatedPlaces().size());
        createResponse.getCreatedPlaces().forEach(place -> {
            System.out.println("\n  📍 " + place.getName());
            System.out.println("     - 주소: " + place.getAddress());
            System.out.println("     - Google Place ID: " + place.getGooglePlaceId());
            System.out.println("     - 이미지 개수: " + place.getImageCount());
        });

        if (!createResponse.getFailedAddresses().isEmpty()) {
            System.out.println("\n❌ 실패한 주소:");
            createResponse.getFailedAddresses().forEach(addr -> 
                System.out.println("  - " + addr)
            );
        }

        // 검증
        assertThat(createResponse.getReelId()).isEqualTo(testReel.getId());
        
        // Google Places API가 정상 작동한다면 장소가 생성되어야 함
        // API 키가 없거나 실패하면 failedAddresses에 포함됨
        int totalAttempts = createResponse.getCreatedPlaces().size() + 
                           createResponse.getFailedAddresses().size();
        assertThat(totalAttempts).isEqualTo(extractResponse.getAddresses().size());

        // ===== STEP 3: 릴스 상태 확인 =====
        System.out.println("\n\n========== STEP 3: 릴스 상태 확인 ==========");
        
        Reel updatedReel = reelRepository.findById(testReel.getId()).get();
        System.out.println("최종 릴스 상태: " + updatedReel.getStatus());

        // 장소가 하나라도 생성되었으면 PLACE_FOUND
        // 모두 실패했으면 PLACE_NOT_FOUND 또는 NO_ADDRESS
        assertThat(updatedReel.getStatus()).isIn(
                ReelStatus.PLACE_FOUND, 
                ReelStatus.PLACE_NOT_FOUND,
                ReelStatus.NO_ADDRESS
        );

        System.out.println("\n\n========== 테스트 완료 ==========");
    }

    @Test
    @DisplayName("주소 없는 캡션 - NO_ADDRESS 상태")
    void testFlow_noAddress() {
        // given - 주소 없는 캡션으로 릴스 수정
        String captionWithoutAddress = """
                오늘 날씨 너무 좋다!
                #일상 #데일리 #좋아요
                """;
        
        testReel.updateMetadata(testReel.getThumbnailUrl(), captionWithoutAddress);
        reelRepository.save(testReel);

        // when - 주소 추출
        ExtractAddressResponse extractResponse = internalReelService.extractAddresses(testReel.getId());

        // then
        assertThat(extractResponse.getAddresses()).isEmpty();

        // when - 장소 생성 (빈 주소 리스트)
        CreatePlacesRequest createRequest = CreatePlacesRequest.builder()
                .addresses(List.of())
                .build();
        
        CreatePlacesResponse createResponse = internalReelService.createPlaces(
                testReel.getId(), 
                createRequest
        );

        // then
        Reel updatedReel = reelRepository.findById(testReel.getId()).get();
        assertThat(updatedReel.getStatus()).isEqualTo(ReelStatus.NO_ADDRESS);
    }

    @Test
    @DisplayName("복수 주소 추출 테스트")
    void testFlow_multipleAddresses() {
        // given - 여러 주소가 있는 캡션
        String captionWithMultipleAddresses = """
                오늘의 데이트 코스!
                
                1차: 서울 강남구 압구정로 123
                2차: 📍 서울특별시 송파구 잠실동 456-78
                
                둘 다 강추!!
                """;
        
        testReel.updateMetadata(testReel.getThumbnailUrl(), captionWithMultipleAddresses);
        reelRepository.save(testReel);

        // when
        ExtractAddressResponse extractResponse = internalReelService.extractAddresses(testReel.getId());

        // then
        System.out.println("\n=== 복수 주소 추출 결과 ===");
        extractResponse.getAddresses().forEach(addr -> 
            System.out.println("  - " + addr)
        );
        
        assertThat(extractResponse.getAddresses().size()).isGreaterThanOrEqualTo(2);
    }
}
