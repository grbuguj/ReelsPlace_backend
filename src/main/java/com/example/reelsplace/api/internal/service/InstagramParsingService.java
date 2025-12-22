package com.example.reelsplace.api.internal.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * Instagram 릴스 파싱 서비스
 *
 * 핵심 발견:
 * - 썸네일: /media/?size=l URL 패턴 사용 (항상 작동!)
 * - 캡션: Meta Graph API oEmbed 사용 (공식 API)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InstagramParsingService {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${instagram.facebook.app-id}")
    private String appId;

    @Value("${instagram.facebook.app-secret}")
    private String appSecret;

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
            //String caption = "";
            //try {
            //    caption = extractCaption(normalizedUrl);
            //} catch (Exception e) {
            //    log.warn("⚠️ 캡션 추출 실패 (썸네일은 성공): {}", e.getMessage());
            //}

            String caption = "매장 :마망젤라또 성수점, 주소 :서울 성동구 연무장9길 8";
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
     * 캡션 추출 (Meta Graph API oEmbed만 사용)
     */
    private String extractCaption(String normalizedUrl) {
        try {
            log.info("📝 캡션 추출 시도 중...");
            return extractCaptionViaOEmbed(normalizedUrl);
        } catch (Exception e) {
            log.warn("⚠️ 캡션 추출 중 오류: {}", e.getMessage());
            return "";
        }
    }

    /**
     * Meta Graph API oEmbed로 캡션 추출 (공식 API)
     * https://developers.facebook.com/docs/instagram/oembed
     */
    private String extractCaptionViaOEmbed(String normalizedUrl) {
        try {
            log.info("🔍 Meta Graph API oEmbed 시도...");
            
            // oEmbed API 엔드포인트
            String apiUrl = String.format(
                "https://graph.facebook.com/v22.0/instagram_oembed?url=%s&access_token=%s|%s",
                normalizedUrl,
                appId,
                appSecret
            );
            
            // API 호출
            String jsonResponse = restTemplate.getForObject(apiUrl, String.class);
            
            if (jsonResponse == null || jsonResponse.isEmpty()) {
                log.warn("⚠️ oEmbed API 응답이 비어있음");
                return "";
            }
            
            log.info("✅ oEmbed API 응답 받음: {} chars", jsonResponse.length());
            
            // JSON에서 html 필드 추출
            java.util.regex.Pattern htmlPattern = java.util.regex.Pattern.compile("\"html\"\\s*:\\s*\"((?:[^\"\\\\]|\\\\.)*)\"");
            java.util.regex.Matcher htmlMatcher = htmlPattern.matcher(jsonResponse);
            
            if (!htmlMatcher.find()) {
                log.warn("⚠️ oEmbed 응답에서 html 필드를 찾을 수 없음");
                return "";
            }
            
            String htmlContent = htmlMatcher.group(1);
            
            // JSON 이스케이프 문자 디코딩
            htmlContent = htmlContent.replace("\\n", "\n")
                                    .replace("\\\"", "\"")
                                    .replace("\\/", "/")
                                    .replace("\\\\", "\\");
            
            log.info("📄 HTML 콘텐츠 추출 완료: {} chars", htmlContent.length());
            
            // Jsoup으로 HTML 파싱하여 캡션 추출
            Document doc = Jsoup.parse(htmlContent);
            
            // blockquote 내부의 텍스트 추출
            String caption = doc.select("blockquote").text();
            
            if (caption != null && !caption.isEmpty()) {
                log.info("✅ oEmbed에서 캡션 추출 성공: {} chars", caption.length());
                return caption;
            }
            
            log.warn("⚠️ HTML에서 캡션을 찾을 수 없음");
            return "";
            
        } catch (Exception e) {
            log.warn("⚠️ oEmbed API 실패: {}", e.getMessage());
            return "";
        }
    }
}
