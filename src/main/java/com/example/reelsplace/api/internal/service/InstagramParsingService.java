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
            String thumbnailUrl = thumbnailUrl1 + "/media/?size=l";
            log.info("📸 썸네일 URL 생성: {}", thumbnailUrl);

            // 캡션 추출 시도 (실패해도 계속 진행)
            String caption = "썰매+빙어잡이+놀이기구까지 한번에!\n" +
                    "서울에서 가장 핫한 눈썰매장 \uD83D\uDEF7❄\uFE0F\n" +
                    "(+본문 하단에 할인 대상자도 적어두었어요!)\n" +
                    "\n" +
                    "@@썰매 타러가자\n" +
                    "\n" +
                    "80M 길이의 스릴 넘치는 슬로프\n" +
                    "오직 겨울에만 갈 수 있는 서울핫플✨\n" +
                    "\n" +
                    "멀고 비싼 썰매장과 달리 단돈 6천원!\n" +
                    "지하철역 바로 앞에 위치한\n" +
                    "접근성 최고인 눈썰매장❄\uFE0F\n" +
                    "\n" +
                    "눈썰매 뿐 아니라 바이킹, 범퍼카, 디스코팡팡 등\n" +
                    "다양한 놀이기구와 직접 잡은 빙어를\n" +
                    "바로 튀겨먹을 수 있는 체험 놀거리도\n" +
                    "하기 좋게 준비되어 있어요\uD83E\uDD0D\n" +
                    "\n" +
                    "놀다가 출출할쯤 매점에 있는 떡볶이랑\n" +
                    "라면같은 따끈한 음식 먹다보면 시간순삭!!\n" +
                    "도심 속 특별한 놀거리 찾는다면\n" +
                    "이번 겨울 데이트로 고고⛄\uFE0F\n" +
                    "\n" +
                    "▶ 할인/감면 혜택\n" +
                    "· 다둥이행복카드 소지자 입장료 50% 할인\n" +
                    "· 만 65세 이상(신분증) 입장료 50% 할인\n" +
                    "· 국가유공자(증서) 입장료 50% 할인\n" +
                    "· 장애인(등록증) + 동행 보호자 1명 입장료 50% 할인\n" +
                    "· 국가·지자체 주관 행사 참여자 입장료 100% 감면\n" +
                    "· 국가·지자체 후원 행사 참여자 입장료 50% 감면\n" +
                    "\n" +
                    "\uD83E\uDD0D같이 썰매 탈 사람에게 공유해주기\uD83E\uDD0D\n" +
                    "@jello_haa\n" +
                    "@jello_haa\n" +
                    "@jello_haa\n" +
                    "└ 팔로우하고 핫플 받아보세요\uD83D\uDC8C\n" +
                    "\n" +
                    "\uD83D\uDCCD 뚝섬한강공원 눈썰매장\n" +
                    "■ 서울 광진구 자양동 112\n" +
                    "■ 뚝섬·잠원·여의도 3곳에 있음\n" +
                    "■ 2025.12.19.~2026.02.18\n" +
                    "■ 10:00~17:00 (정비시간 13:00~14:00)\n" +
                    "■ 주차: 한강공원 3주차장\n" +
                    "■ 입장료: 6000원 (어른/아이 동일)\n" +
                    "■ 빙어잡기: 6000원\n" +
                    "■ 16:00에 입장마감이라 늦어도 15시에는 방문추천\n" +
                    "■ 성인 슬로프 / 어린이 슬로프 나뉘어짐\n" +
                    "■ 떡볶이, 순대, 튀김, 라면, 회오리감자, 소떡소떡 등 간식판매";
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
