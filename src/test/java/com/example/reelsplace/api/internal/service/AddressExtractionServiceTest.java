package com.example.reelsplace.api.internal.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 주소 추출 서비스 테스트
 */
@SpringBootTest
class AddressExtractionServiceTest {

    @Autowired
    private AddressExtractionService addressExtractionService;

    @Test
    @DisplayName("주소: 패턴 - 주소 추출 성공")
    void extractAddresses_withAddressPattern() {
        // given
        String caption = """
                오늘 다녀온 맛집!
                주소 : 서울특별시 강남구 테헤란로 123
                분위기 좋고 음식도 맛있어요 👍
                """;

        // when
        List<String> addresses = addressExtractionService.extractAddresses(caption);

        // then
        assertThat(addresses).hasSize(1);
        assertThat(addresses.get(0)).contains("서울특별시 강남구 테헤란로 123");
    }

    @Test
    @DisplayName("위치: 패턴 - 주소 추출 성공")
    void extractAddresses_withLocationPattern() {
        // given
        String caption = """
                강남 핫플 발견!
                위치: 서울 강남구 역삼동 123-45
                #강남맛집 #데이트코스
                """;

        // when
        List<String> addresses = addressExtractionService.extractAddresses(caption);

        // then
        assertThat(addresses).hasSize(1);
        assertThat(addresses.get(0)).contains("서울 강남구 역삼동");
    }

    @Test
    @DisplayName("📍 이모지 패턴 - 주소 추출 성공")
    void extractAddresses_withEmojiPattern() {
        // given
        String caption = """
                여기 진짜 맛있어요!
                📍 서울특별시 마포구 연남동 369-11
                꼭 가보세요!
                """;

        // when
        List<String> addresses = addressExtractionService.extractAddresses(caption);

        // then
        assertThat(addresses).hasSize(1);
        assertThat(addresses.get(0)).contains("서울특별시 마포구 연남동");
    }

    @Test
    @DisplayName("@ 태그 패턴 - 주소 추출 성공")
    void extractAddresses_withAtPattern() {
        // given
        String caption = """
                주말 데이트 코스 추천
                @강남구 신사동 123-45
                분위기 좋아요!
                """;

        // when
        List<String> addresses = addressExtractionService.extractAddresses(caption);

        // then
        assertThat(addresses).hasSize(1);
        assertThat(addresses.get(0)).contains("강남구 신사동");
    }

    @Test
    @DisplayName("일반 주소 패턴 - 주소 추출 성공")
    void extractAddresses_withGeneralPattern() {
        // given
        String caption = """
                오늘 다녀온 카페
                서울특별시 종로구 삼청동 35-1에 위치해있어요
                조용하고 좋아요
                """;

        // when
        List<String> addresses = addressExtractionService.extractAddresses(caption);

        // then
        assertThat(addresses).hasSize(1);
        assertThat(addresses.get(0)).contains("서울특별시 종로구 삼청동");
    }

    @Test
    @DisplayName("복수 주소 추출 - 여러 개 주소가 있을 때")
    void extractAddresses_multipleAddresses() {
        // given
        String caption = """
                오늘의 데이트 코스!
                
                1차: 서울 강남구 압구정로 123
                2차: 📍 서울특별시 송파구 잠실동 456-78
                
                둘 다 강추!!
                """;

        // when
        List<String> addresses = addressExtractionService.extractAddresses(caption);

        // then
        assertThat(addresses).hasSizeGreaterThanOrEqualTo(2);
    }

    @Test
    @DisplayName("주소 없음 - 빈 리스트 반환")
    void extractAddresses_noAddress() {
        // given
        String caption = """
                오늘 날씨 너무 좋다!
                #일상 #데일리 #좋아요
                """;

        // when
        List<String> addresses = addressExtractionService.extractAddresses(caption);

        // then
        assertThat(addresses).isEmpty();
    }

    @Test
    @DisplayName("너무 짧은 주소 - 필터링됨 (5자 미만)")
    void extractAddresses_tooShort() {
        // given
        String caption = "주소: 서울";

        // when
        List<String> addresses = addressExtractionService.extractAddresses(caption);

        // then
        assertThat(addresses).isEmpty();
    }

    @Test
    @DisplayName("해시태그와 @ 제거 - cleanAddress 동작 확인")
    void extractAddresses_cleanAddress() {
        // given
        String caption = "위치: #서울특별시 강남구 테헤란로 123";

        // when
        List<String> addresses = addressExtractionService.extractAddresses(caption);

        // then
        assertThat(addresses).hasSize(1);
        assertThat(addresses.get(0)).doesNotContain("#");
    }

    @Test
    @DisplayName("실전 Mock 캡션 - 전체 플로우 테스트")
    void extractAddresses_realWorldMockCaption() {
        // given
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

        // when
        List<String> addresses = addressExtractionService.extractAddresses(mockCaption);

        // then
        assertThat(addresses).isNotEmpty();
        assertThat(addresses).anyMatch(addr -> addr.contains("마포구 연남동"));
        
        // 로그 확인용
        System.out.println("\n=== 추출된 주소 목록 ===");
        addresses.forEach(addr -> System.out.println("- " + addr));
    }
}
