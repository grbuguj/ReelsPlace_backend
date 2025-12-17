package com.example.reelsplace.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * 비동기 처리 설정
 */
@Configuration
@EnableAsync
public class AsyncConfig {
    // Spring의 기본 ThreadPoolTaskExecutor 사용
}
