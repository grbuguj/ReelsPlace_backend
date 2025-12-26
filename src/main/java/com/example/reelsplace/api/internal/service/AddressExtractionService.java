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
            Pattern.compile(
                    "(" +
                            // 1️⃣ 시/도 (서울, 경기, 부산, 전라남도 등)
                            "(?:서울|부산|대구|인천|광주|대전|울산|세종|제주|[가-힣]+(?:도|특별시|광역시))\\s*" +

                            // 2️⃣ 시/군/구 (있을 수도, 없을 수도)
                            "(?:[가-힣]+(?:시|군|구)\\s*)*" +

                            // 3️⃣ 동/읍/면/로/길 (핵심)
                            "[가-힣0-9]+(?:동|읍|면|로|길)\\s*" +

                            // 4️⃣ 번지 (있을 수도 없음)
                            "[0-9-]*" +
                            ")"
            )
    );



    private static final List<Pattern> PLACE_NAME_PATTERNS = List.of(
            // 1️⃣ 명시적
            Pattern.compile("매장(?:명)?\\s*[:\\：]\\s*([^,\\n]+)"),

            // 2️⃣ 📍 강릉길감자
            Pattern.compile("📍\\s*([가-힣0-9A-Za-z]+)"),

            // 3️⃣ '강릉길감자'
            Pattern.compile("[\"'‘’]([가-힣0-9A-Za-z]+)[\"'‘’]"),

            // 4️⃣ 강릉길감자 (중앙시장)
            Pattern.compile("([가-힣0-9A-Za-z]+)\\s*\\(")
    );




    /**
     * 캡션에서 주소 추출
     * @param caption 릴스 캡션
     * @return 추출된 주소 리스트
     */

    public Optional<String> extractPlaceNameNearAddress(String caption, String address) {
        int idx = caption.indexOf(address);
        if (idx <= 0) return Optional.empty();

        int start = Math.max(0, idx - 15);
        String candidate = caption.substring(start, idx).trim();

        // 마지막 단어만 사용
        String[] tokens = candidate.split("\\s+");
        String last = tokens[tokens.length - 1];

        if (last.length() >= 2 && isValidPlaceName(last)) {
            return Optional.of(last);
        }
        return Optional.empty();
    }


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




