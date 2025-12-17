package com.example.reelsplace.domain.enums;

/**
 * 릴스 파싱 상태
 * ERD: Reel.status
 */
public enum ReelStatus {
    PROCESSING,      // 파싱 중
    NO_ADDRESS,      // 주소 없음
    PLACE_FOUND,     // 장소 생성됨
    PLACE_NOT_FOUND, // 장소 못 찾음
    FAILED           // 파싱 실패
}
