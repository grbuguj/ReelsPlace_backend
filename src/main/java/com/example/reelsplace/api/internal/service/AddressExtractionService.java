package com.example.reelsplace.api.internal.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 캡션에서 주소 추출 서비스
 */
@Slf4j
@Service
public class AddressExtractionService {

    // 주소 패턴 정규식
    private static final List<Pattern> ADDRESS_PATTERNS = List.of(
            // "주소 : ", "주소: ", "주소 :", "주소:"
            Pattern.compile("주소\\s*[:\\：]\\s*([^\\n]+)"),
            // "위치 : ", "위치: ", "위치 :", "위치:"
            Pattern.compile("위치\\s*[:\\：]\\s*([^\\n]+)"),
            // 📍 이모지 뒤 주소
            Pattern.compile("\uD83D\uDCCD\\s*([^\\n]+)"),
            // @ 태그 형식 (예: @서울 강남구...)
            Pattern.compile("@([가-힣\\s]+(?:구|동|로|길)\\s*[0-9-]+[^\\n]*)"),
            // 일반 한국 주소 패턴 (시/도 + 구/군 + 동/읍/면)
            Pattern.compile("([가-힣]+(?:특별시|광역시|시|도)\\s+[가-힣]+(?:구|군)\\s+[가-힣]+(?:동|읍|면|로|길)\\s*[0-9-]*)")
    );

    private static final List<Pattern> PLACE_NAME_PATTERNS = List.of(
            // "매장 : OOO", "매장명: OOO"
            Pattern.compile("매장(?:명)?\\s*[:\\：]\\s*([^,\\n]+)"),

            // "카페 OOO", "맛집 OOO", "식당 OOO"
            Pattern.compile("(?:카페|맛집|식당|바|술집)\\s+([가-힣A-Za-z0-9\\s]+)"),

            // 따옴표 안 매장명: "OOO", ‘OOO’
            Pattern.compile("[\"“”‘’']\\s*([^\"“”‘’']{2,30})\\s*[\"“”‘’']"),

            // 📍 OOO (주소 말고 상호만 있는 경우)
            Pattern.compile("\uD83D\uDCCD\\s*([가-힣A-Za-z0-9\\s]{2,30})"),

            // 첫 줄 단독 매장명 (줄바꿈 전)
            Pattern.compile("^([가-힣A-Za-z0-9\\s]{2,30})\\n")
    );


    /**
     * 캡션에서 주소 추출
     * @param caption 릴스 캡션
     * @return 추출된 주소 리스트
     */
    public List<String> extractAddresses(String caption) {
        if (caption == null || caption.isBlank()) {
            log.debug("캡션이 비어있음");
            return List.of();
        }

        List<String> addresses = new ArrayList<>();

        // 각 패턴으로 주소 추출
        for (Pattern pattern : ADDRESS_PATTERNS) {
            Matcher matcher = pattern.matcher(caption);
            while (matcher.find()) {
                String address = cleanAddress(matcher.group(1));
                
                // 최소 길이 체크 (너무 짧은 주소 제외)
                if (address.length() >= 5 && !addresses.contains(address)) {
                    addresses.add(address);
                    log.debug("주소 추출 성공: {}", address);
                }
            }
        }

        log.info("총 {}개 주소 추출 완료", addresses.size());
        return addresses;
    }
    public Optional<String> extractPlaceName(String caption) {
        if (caption == null || caption.isBlank()) {
            log.debug("캡션이 비어있음 (매장명 추출 불가)");
            return Optional.empty();
        }

        for (Pattern pattern : PLACE_NAME_PATTERNS) {
            Matcher matcher = pattern.matcher(caption);
            if (matcher.find()) {
                String placeName = cleanPlaceName(matcher.group(1));

                // 너무 짧거나 애매한 값 필터링
                if (placeName.length() >= 2 && isValidPlaceName(placeName)) {
                    log.debug("매장명 추출 성공: {}", placeName);
                    return Optional.of(placeName);
                }
            }
        }

        log.debug("매장명 추출 실패");
        return Optional.empty();
    }



    /**
     * 주소 정제 (불필요한 문자 제거)
     */
    private String cleanAddress(String address) {
        return address
                .replaceAll("[#@]", "")  // 해시태그, @ 제거
                .replaceAll("\\s+", " ")  // 여러 공백을 하나로
                .trim();
    }

    /**
     * 매장명 정제
     */
    private String cleanPlaceName(String placeName) {
        return placeName
                .replaceAll("[#@]", "")      // 해시태그, @ 제거
                .replaceAll("\\s+", " ")     // 공백 정리
                .replaceAll("^(카페|맛집|식당|바)\\s*", "") // 접두 키워드 제거
                .trim();
    }
    /**
     * 매장명 유효성 판단
     */
    private boolean isValidPlaceName(String name) {
        // 너무 일반적인 단어 제거
        List<String> blacklist = List.of(
                "오늘", "여기", "진짜", "추천", "데이트",
                "맛집", "카페", "식당", "술집", "분위기",
                "핫플", "코스"
        );

        for (String word : blacklist) {
            if (name.equalsIgnoreCase(word)) {
                return false;
            }
        }

        return true;
    }


}




