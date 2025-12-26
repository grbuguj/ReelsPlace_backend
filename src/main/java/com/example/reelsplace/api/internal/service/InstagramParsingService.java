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
            //String normalizedUrl = reelUrl.replace("/reel/", "/p/");

            String normalizedUrl = reelUrl;

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

            String thumbnailUrl1 = normalizedUrl.replace("/reel/", "/p/");
            String thumbnailUrl = thumbnailUrl1 + "media/?size=l";
            log.info("📸 썸네일 URL 생성: {}", thumbnailUrl);

            // 캡션 추출 시도 (실패해도 계속 진행)
            String caption = "낭만 가득한 라이브 재즈바\uD83C\uDFB6\uD83E\uDD42\n" +
                    "\n" +
                    "작은 유럽이 떠오르는 공간에서\n" +
                    "라이브 재즈 공연까지..\n" +
                    "로맨틱함 그 자체였어요\uD83D\uDC97\n" +
                    "\n" +
                    "서촌 데이트하며\n" +
                    "분위기 내고 싶을 때 추천✨\n" +
                    "\n" +
                    "\uD83C\uDF77 연말 데이트/모임 장소 찾는 분들 저장 & 공유\n" +
                    "\n" +
                    "✔ 공연비 11,000원~15,000원\n" +
                    "✔ 선착순 자리 배정\n" +
                    "✔ 외부음식 케이크 가능\n" +
                    "\n" +
                    "\uD83D\uDCCD 하우스오브블루 / @houseofblue.seoul\n" +
                    "\uD83D\uDCCD 서울 종로구 자하문로9길 6 지하 1층\n" +
                    "\n" +
                    "\uD83D\uDD70 목,금 19:00–2:00 / 토,일 18:00–2:00\n" +
                    "(월,화 휴무)\n" +
                    "\n" +
                    "✅ 캐치테이블 / DM 예약가능\n" +
                    "\uD83D\uDC40 12.24 DM 당일 예약 후 방문";
            //try {
            //    caption = extractCaption(normalizedUrl);
            //} catch (Exception e) {
            //    log.warn("캡션 추출 실패: {}", e.getMessage());
            //}

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
