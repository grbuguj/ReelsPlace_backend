package com.example.reelsplace.api.internal.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

/**
 * Instagram 릴스 파싱 서비스
 *
 * 핵심 발견:
 * - 썸네일: /media/?size=l URL 패턴 사용 (항상 작동!)
 * - 캡션: HTML 파싱 시도 (실패 시 빈 문자열)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InstagramParsingService {

    /**
     * 릴스 URL에서 메타데이터 파싱
     *
     * @param reelUrl Instagram 릴스 URL
     * @return [썸네일URL, 캡션]
     */
    public String[] parseReelMetadata(String reelUrl) {
        try {
            // /reel/을 /p/로 변환
            String normalizedUrl = reelUrl.replace("/reel/", "/p/");
            
            // URL 끝의 쿼리 파라미터 제거 (있다면)
            if (normalizedUrl.contains("?")) {
                normalizedUrl = normalizedUrl.substring(0, normalizedUrl.indexOf("?"));
            }
            
            // URL 끝의 / 제거 (있다면)
            if (normalizedUrl.endsWith("/")) {
                normalizedUrl = normalizedUrl.substring(0, normalizedUrl.length() - 1);
            }

            log.info("🔍 Instagram 파싱 시작 - URL: {}", normalizedUrl);

            // ✅ 썸네일: /media/?size=l 패턴 사용 (항상 작동!)
            String thumbnailUrl = normalizedUrl + "/media/?size=l";
            log.info("📸 썸네일 URL 생성: {}", thumbnailUrl);

            // 캡션 추출 시도 (실패해도 계속 진행)
            String caption = "";
            try {
                caption = extractCaption(normalizedUrl);
            } catch (Exception e) {
                log.warn("⚠️ 캡션 추출 실패 (썸네일은 성공): {}", e.getMessage());
            }

            log.info("✅ 파싱 완료!");
            log.info("📸 썸네일: {}", thumbnailUrl);
            log.info("📝 캡션: {}", caption.isEmpty() ? "(없음)" : 
                    (caption.length() > 100 ? caption.substring(0, 100) + "..." : caption));

            return new String[]{
                    thumbnailUrl.trim(),
                    caption.trim()
            };

        } catch (Exception e) {
            log.error("❌ Instagram 파싱 실패 - URL: {}, Error: {}", reelUrl, e.getMessage());
            throw new RuntimeException("Instagram 파싱 실패: " + e.getMessage(), e);
        }
    }

    /**
     * 캡션 추출 (HTML 파싱 시도)
     * 실패해도 빈 문자열 반환
     */
    private String extractCaption(String normalizedUrl) {
        try {
            log.info("📝 캡션 추출 시도 중...");
            
            // 방법 1: ?__a=1&__d=dis 엔드포인트 시도 (JSON)
            String caption = extractCaptionViaJson(normalizedUrl);
            if (!caption.isEmpty()) {
                return caption;
            }
            
            // 방법 2: HTML 파싱 폴백
            caption = extractCaptionViaHtml(normalizedUrl);
            return caption;

        } catch (Exception e) {
            log.warn("⚠️ 캡션 추출 중 오류: {}", e.getMessage());
            return "";
        }
    }

    /**
     * JSON 엔드포인트로 캡션 추출
     * https://www.instagram.com/p/{shortcode}?__a=1&__d=dis
     */
    private String extractCaptionViaJson(String normalizedUrl) {
        try {
            log.info("🔍 JSON 엔드포인트 시도...");
            
            String jsonUrl = normalizedUrl + "?__a=1&__d=dis";
            
            Document doc = Jsoup.connect(jsonUrl)
                    .userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36")
                    .header("X-IG-App-ID", "936619743392459")
                    .header("Accept", "application/json")
                    .ignoreContentType(true) // JSON 응답 허용
                    .timeout(10000)
                    .get();
            
            // JSON 파싱
            String json = doc.text();
            
            // 간단한 JSON 파싱 (caption 필드 찾기)
            // 정규식으로 "caption":"..." 추출
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\"caption\"\\s*:\\s*\"([^\"]+)\"");
            java.util.regex.Matcher matcher = pattern.matcher(json);
            
            if (matcher.find()) {
                String caption = matcher.group(1);
                // JSON 이스케이프 문자 디코딩
                caption = caption.replace("\\n", "\n")
                                .replace("\\\"", "\"")
                                .replace("\\\\", "\\");
                
                log.info("✅ JSON 엔드포인트에서 캡션 추출 성공!");
                return caption;
            }
            
            log.warn("⚠️ JSON에서 캡션 필드를 찾을 수 없음");
            return "";
            
        } catch (Exception e) {
            log.warn("⚠️ JSON 엔드포인트 실패: {}", e.getMessage());
            return "";
        }
    }

    /**
     * HTML 파싱으로 캡션 추출 (폴백)
     */
    private String extractCaptionViaHtml(String normalizedUrl) {
        try {
            log.info("🔍 HTML 파싱 폴백 시도...");

            Document doc = Jsoup.connect(normalizedUrl)
                    .userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36")
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8")
                    .header("Accept-Language", "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7")
                    .timeout(10000)
                    .get();

            // description 메타 태그에서 캡션 추출
            String description = doc.select("meta[name=description]").attr("content");
            if (description != null && !description.trim().isEmpty()) {
                log.info("✓ HTML description 발견: {} chars", description.length());
                return description;
            }

            // og:description 폴백
            description = doc.select("meta[property=og:description]").attr("content");
            if (description != null && !description.trim().isEmpty()) {
                log.info("✓ og:description 발견: {} chars", description.length());
                return description;
            }

            log.warn("⚠️ HTML에서 캡션을 찾을 수 없음");
            return "";

        } catch (Exception e) {
            log.warn("⚠️ HTML 파싱 실패: {}", e.getMessage());
            return "";
        }
    }
}
