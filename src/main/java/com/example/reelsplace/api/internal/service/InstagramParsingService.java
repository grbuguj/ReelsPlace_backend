package com.example.reelsplace.api.internal.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

/**
 * Instagram 릴스 파싱 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InstagramParsingService {

    /**
     * 릴스 URL에서 메타데이터 파싱
     * @return [썸네일URL, 캡션]
     */
    public String[] parseReelMetadata(String reelUrl) {
        try {
            // Instagram 릴스 URL을 /p/ 형식으로 변환
            String embedUrl = convertToEmbedUrl(reelUrl);
            
            // User-Agent 설정하여 HTML 가져오기
            Document doc = Jsoup.connect(embedUrl)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .timeout(10000)
                    .get();
            
            // og:image (썸네일)
            String thumbnailUrl = doc.select("meta[property=og:image]")
                    .attr("content");
            
            // og:description (캡션)
            String caption = doc.select("meta[property=og:description]")
                    .attr("content");
            
            log.info("릴스 파싱 성공 - URL: {}", reelUrl);
            
            return new String[]{thumbnailUrl, caption};
            
        } catch (Exception e) {
            log.error("릴스 파싱 실패 - URL: {}, Error: {}", reelUrl, e.getMessage());
            throw new RuntimeException("릴스 파싱 실패", e);
        }
    }

    /**
     * 릴스 URL을 embed URL로 변환
     * https://www.instagram.com/reel/xxxxx/ → https://www.instagram.com/p/xxxxx/
     */
    private String convertToEmbedUrl(String reelUrl) {
        return reelUrl.replace("/reel/", "/p/");
    }
}
