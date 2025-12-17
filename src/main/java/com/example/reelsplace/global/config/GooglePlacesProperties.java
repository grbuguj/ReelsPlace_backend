package com.example.reelsplace.global.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Google Places API 설정
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "google.places")
public class GooglePlacesProperties {
    private String apiKey;
    private String baseUrl;
}
