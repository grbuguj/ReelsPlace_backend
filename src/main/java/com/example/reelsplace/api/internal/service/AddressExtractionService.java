package com.example.reelsplace.api.internal.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
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
                String address = matcher.group(1).trim();
                
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

    /**
     * 주소 정제 (불필요한 문자 제거)
     */
    private String cleanAddress(String address) {
        return address
                .replaceAll("[#@]", "")  // 해시태그, @ 제거
                .replaceAll("\\s+", " ")  // 여러 공백을 하나로
                .trim();
    }
}
